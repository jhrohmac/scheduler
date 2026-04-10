package com.scheduler.comm.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;

public class ServerResourceMonitor {
    public static void main(String[] args) {
        DecimalFormat df = new DecimalFormat("#.##");
        
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();

        while (true) {
            // CPU 사용률 (Process CPU Load 및 System Load Average)
            double loadAverage = osBean.getSystemLoadAverage();
            int availableProcessors = osBean.getAvailableProcessors();

            // 메모리 사용량 계산
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();

            double memUsagePercent = ((double) usedMemory / maxMemory) * 100;

            // 결과 출력
            System.out.println("===========================================");
            System.out.println("CPU 사용률: " + df.format(loadAverage / availableProcessors * 100) + " %");
            System.out.println("메모리 사용률: " + df.format(memUsagePercent) + " %");
            System.out.println("사용된 메모리: " + usedMemory / (1024 * 1024) + " MB");
            System.out.println("최대 메모리: " + maxMemory / (1024 * 1024) + " MB");
            System.out.println("===========================================");

            // 1초 간격으로 모니터링
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
