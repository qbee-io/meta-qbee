import time
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

    @OETestDepends(['qbee.QbeeAgentTest.test_qbee_service_enabled'])
    def test_qbee_service_active(self):
        """Verify that the systemd service started successfully."""
        status, output = self.target.run('systemctl is-active qbee-agent.service')
        self.assertEqual(status, 0, msg=f"qbee-agent service is not active/running: {output}")

    # @OETestDepends(['qbee.QbeeAgentTest.test_qbee_service_active'])
    # def test_qbee_service_restart(self):
    #     """Verify that the service can be restarted gracefully."""
    #     status, output = self.target.run('systemctl restart qbee-agent.service')
    #     self.assertEqual(status, 0, msg=f"Failed to restart qbee-agent service: {output}")
        
    #     time.sleep(2)
        
    #     status, output = self.target.run('systemctl is-active qbee-agent.service')
    #     self.assertEqual(status, 0, msg=f"qbee-agent service crashed after restart: {output}")

    # @OETestDepends(['qbee.QbeeAgentTest.test_qbee_service_active'])
    # def test_qbee_journal_logs(self):
    #     """Scan the journal logs to ensure the agent isn't panic-looping."""
    #     status, output = self.target.run('journalctl -u qbee-agent.service --no-pager')
    #     self.assertEqual(status, 0, msg="Failed to retrieve journalctl logs for qbee-agent.")

    #     critical_errors = ["panic", "FATAL", "segmentation fault"]
    #     for error in critical_errors:
    #         self.assertNotIn(error, output, msg=f"Critical error '{error}' found in qbee-agent logs:\n{output}")