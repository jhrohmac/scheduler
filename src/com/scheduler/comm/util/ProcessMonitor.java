package com.scheduler.comm.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessMonitor {
    public static void main(String[] args) {
        try {
            String user = "xtm"; // 모니터링할 사용자 이름
            String command = "top -b -n 1"; // top 명령어를 1회 실행
            Pattern pattern = Pattern.compile(
                "\\s*(\\d+)\\s+" + user + "\\s+.*?\\s+(\\d+\\.\\d)\\s+(\\d+\\.\\d)\\s+(\\d+:\\d+\\.\\d+)\\s+(\\S+)"
            ); // 정규 표현식

            System.out.println("=============command======================");
            System.out.println(command);
            while (true) {
                Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                System.out.println("PID\tUSER\tCPU%\tMEM%\tTIME+\tCOMMAND");
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String pid = matcher.group(1);
                        String cpu = matcher.group(2);
                        String mem = matcher.group(3);
                        String time = matcher.group(4);
                        String cmd = matcher.group(5);

                        System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\n", pid, user, cpu, mem, time, cmd);
                    }
                }

                reader.close();
                Thread.sleep(2000); // 2초마다 갱신
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
