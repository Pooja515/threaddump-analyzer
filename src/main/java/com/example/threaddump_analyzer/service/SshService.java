package com.example.threaddump_analyzer.service;

import com.jcraft.jsch.*;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
public class SshService {

    public String executeRemoteJstack(String host, String port, String username, String password, String pid) throws Exception {
        Session session = null;
        ChannelExec channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, Integer.parseInt(port));
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(10_000);

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("jstack " + pid);
            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            return output.toString();
        } catch (JSchException e) {
            throw new RuntimeException("SSH connection failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to capture thread dump: " + e.getMessage(), e);
        } finally {
            if (channel != null && !channel.isClosed()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
