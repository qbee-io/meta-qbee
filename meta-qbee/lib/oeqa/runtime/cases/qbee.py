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
    """ Copy the qbee-agent bootstrap and run shellcheck the script to verify it executes without error."""
    tmpPath = tempfile.NamedTemporaryFile(delete=False)

    self.target.copy_from('/etc/qbee/yocto/qbee-bootstrap-prep.sh', tmpPath.name)
    status, output = self.target.run(f'shellcheck {tmpPath.name}')
    self.assertEqual(status, 0, msg=f"qbee-bootstrap-prep.sh has shellcheck errors: {output}")
