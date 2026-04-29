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
    bootstrapEnvTargetPath = "/etc/qbee/yocto/.bootstrap-env"
    qbeeAgentConfigFile = "/data/qbee/etc/qbee-agent.json"

    lines = [
      "BOOTSTRAP_KEY=my-key",
      "DEVICE_NAME_TYPE=machine-id",
      "DEVICE_HUB_HOST=device.app.qbee.io",
      "DISABLE_REMOTE_ACCESS=true",
      "TPM_DEVICE=/dev/tpm0",
      "CA_CERT=/etc/qbee/ca_cert.pem",
      "ELEVATION_COMMAND='[\"/usr/bin/sudo\", \"-n\"]'",
    ]

    for line in lines:
      bootstrapEnvFile.write(f"{line}\n".encode('utf-8'))
      bootstrapEnvFile.flush()


      status, output = self.target.copyTo(bootstrapEnvFile.name, bootstrapEnvTargetPath)
      self.assertEqual(status, 0, msg=f"Failed to copy bootstrap env file: {output}")
      
      status, output = self.target.run(f'sync')
      self.assertEqual(status, 0, msg=f"Failed to sync files: {output}")

      status, output = self.target.run(f'/etc/qbee/yocto/qbee-bootstrap-prep.sh 2>&1')
      self.assertEqual(status, 0, msg=f"qbee-bootstrap-prep.sh failed to execute with env {line}: {output}")

      status, output = self.target.copyFrom(qbeeAgentConfigFile, qbeeAgentJsonFile.name)
      self.assertEqual(status, 0, msg=f"Failed to copy qbee-agent.json file: {output}")

      """ Verify that the json file is parseable and contains the expected key based on the env variable set. """
      data = json.loads(open(qbeeAgentJsonFile.name, 'r').read())
      self.assertIsNotNone(data, msg=f"Failed to parse qbee-agent.json file with env {line}")

      self.target.run(f'rm -f {bootstrapEnvTargetPath} && rm -f {qbeeAgentConfigFile}')
      
