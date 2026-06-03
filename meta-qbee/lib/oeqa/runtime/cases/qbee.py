import json
import tempfile

from oeqa.runtime.case import OERuntimeTestCase
from oeqa.core.decorator.depends import OETestDepends
from oeqa.runtime.decorator.package import OEHasPackage

class QbeeAgentTest(OERuntimeTestCase):
  @OEHasPackage(['qbee-agent'])
  @OETestDepends(['ssh.SSHTest.test_ssh'])
  def test_qbee_binary_execution(self):
    """Verify the qbee-agent binary exists, has correct permissions, and executes."""
    status, _ = self.target.run('test -x /usr/bin/qbee-agent')
    self.assertEqual(status, 0, msg="qbee-agent binary is missing or not executable.")

    status, output = self.target.run('qbee-agent --help')
    self.assertEqual(status, 0, msg=f"qbee-agent failed to execute: {output}")

  @OETestDepends(['qbee.QbeeAgentTest.test_qbee_binary_execution'])
  def test_qbee_config_directory(self):
    """Verify that the configuration directory was created."""
    status, _ = self.target.run('test -d /etc/qbee')
    self.assertEqual(status, 0, msg="/etc/qbee directory does not exist.")

  @OETestDepends(['qbee.QbeeAgentTest.test_qbee_binary_execution'])
  def test_qbee_service_enabled(self):
    """Verify that the systemd service is enabled to start on boot."""
    status, output = self.target.run('systemctl is-enabled qbee-agent.service')
    self.assertEqual(status, 0, msg=f"qbee-agent service is not enabled: {output}")

  @OETestDepends(['qbee.QbeeAgentTest.test_qbee_binary_execution'])
  def test_qbee_agent_bootstrap_pre_script(self):
    """ Create a bootstrap env file with all available options to verify that the script can handle them without error. """
    bootstrapEnvFile = tempfile.NamedTemporaryFile(delete=False)
    qbeeAgentJsonFile = tempfile.NamedTemporaryFile(delete=False)
    qbeeAgentJsonFile.close()
    bootstrapEnvTargetPath = "/etc/qbee/yocto/.bootstrap-env"
    qbeeAgentConfigFile = "/data/qbee/etc/qbee-agent.json"

    test_cases = [
      ("BOOTSTRAP_KEY=my-key", "bootstrap_key", "my-key"),
      ("DEVICE_NAME_TYPE=machine-id", "device_name", ""),
      ("DEVICE_HUB_HOST=device.app.qbee.io", "server", "device.app.qbee.io"),
      ("DISABLE_REMOTE_ACCESS=true", "disable_remote_access", True),
      ("TPM_DEVICE=/dev/tpm0", "tpm_device", "/dev/tpm0"),
      ("CA_CERT=/etc/qbee/ca_cert.pem", "ca_cert", "/etc/qbee/ca_cert.pem"),
      ("ELEVATION_COMMAND='[\"/usr/bin/sudo\", \"-n\"]'", "elevation_command", ["/usr/bin/sudo", "-n"]),
    ]

    for line, expected_key, expected_value in test_cases:
      bootstrapEnvFile.write(f"{line}\n".encode('utf-8'))
      bootstrapEnvFile.flush()

      status, output = self.target.copyTo(bootstrapEnvFile.name, bootstrapEnvTargetPath)
      self.assertEqual(status, 0, msg=f"Failed to copy bootstrap env file: {output}")
      
      status, output = self.target.run('sync')
      self.assertEqual(status, 0, msg=f"Failed to sync files: {output}")

      status, output = self.target.run('/etc/qbee/yocto/qbee-bootstrap-prep.sh 2>&1')
      self.assertEqual(status, 0, msg=f"qbee-bootstrap-prep.sh failed to execute with env {line}: {output}")

      status, output = self.target.copyFrom(qbeeAgentConfigFile, qbeeAgentJsonFile.name)
      self.assertEqual(status, 0, msg=f"Failed to copy qbee-agent.json file: {output}")

      # Verify that the JSON file is parseable.
      with open(qbeeAgentJsonFile.name, 'r') as qbeeAgentJson:
        data = json.load(qbeeAgentJson)
        self.assertIsNotNone(data, msg=f"Failed to parse qbee-agent.json file with env {line}")
        if expected_key:
          self.assertIn(expected_key, data, msg=f"Expected key '{expected_key}' missing from qbee-agent.json for env {line}")
          if expected_value:
            self.assertEqual(data[expected_key], expected_value, msg=f"Unexpected value for key '{expected_key}' in qbee-agent.json for env {line}")

      self.target.run(f'rm -f {bootstrapEnvTargetPath} && rm -f {qbeeAgentConfigFile}')

  @OETestDepends(['qbee.QbeeAgentTest.test_qbee_agent_bootstrap_pre_script'])
  def test_qbee_agent_systemd_integration(self):
    """Temporarily replace qbee-agent and verify the systemd unit can start/stop it."""
    backup_path = "/usr/bin/qbee-agent.oeqa.bak"
    status, output = self.target.run(f"cp /usr/bin/qbee-agent {backup_path}")
    self.assertEqual(status, 0, msg=f"Failed to backup qbee-agent binary: {output}")

    try:
      testScript = tempfile.NamedTemporaryFile(delete=False)
      testScript.write(b"#!/bin/sh\nwhile true; do echo 'test'; sleep 1; done\n")
      testScript.flush()

      status, output = self.target.copyTo(testScript.name, "/usr/bin/qbee-agent")
      self.assertEqual(status, 0, msg=f"Failed to install test qbee-agent binary: {output}")
      status, output = self.target.run("chmod +x /usr/bin/qbee-agent")
      self.assertEqual(status, 0, msg=f"Failed to chmod test qbee-agent binary: {output}")

      # Create an env file so qbee-agent.json gets generated
      bootstrapEnvFile = tempfile.NamedTemporaryFile(delete=False)
      bootstrapEnvFile.write(b"BOOTSTRAP_KEY=my-key\n")
      bootstrapEnvFile.flush()
      status, output = self.target.copyTo(bootstrapEnvFile.name, "/etc/qbee/yocto/.bootstrap-env")
      self.assertEqual(status, 0, msg=f"Failed to copy bootstrap env file: {output}")
      self.target.run('sync')

      # Start the service and verify that it is running
      self.target.run('systemctl start qbee-agent.service')
      status, output = self.target.run('systemctl is-active qbee-agent.service')
      self.assertEqual(status, 0, msg=f"qbee-agent service failed to start: {output}")

      # Stop the service and verify that it is stopped
      self.target.run('systemctl stop qbee-agent.service')
      status, output = self.target.run('systemctl is-active qbee-agent.service')
      self.assertNotEqual(status, 0, msg=f"qbee-agent service failed to stop: {output}")
    finally:
      self.target.run(f"mv -f {backup_path} /usr/bin/qbee-agent")
      self.target.run("chmod +x /usr/bin/qbee-agent")
