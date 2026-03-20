import java.time.Duration;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

class person {
    AtomicInteger p = new AtomicInteger(0);
    LocalTime  start;
    long duration;          //一次开始到结束的时间
}

public class Menu {
    private static final Object scannerLock = new Object();

    public static void main(String[] args) {
        person p=new person();
        Scanner sc = new Scanner(System.in);
           //  菜单循环开始
        while (true) {
            System.out.println("Enter number for interaction");
            System.out.println("1 to start,2 to end");
            int interaction;
            interaction = sc.nextInt();
            if (interaction ==1) {
                p.start = LocalTime.now();
                //   开始后每分钟报时间，直到输入2结束
                while (true) {
                    LocalTime end = LocalTime.now();
                    p.duration = (Duration.between(p.start, end).getSeconds() + 59) / 60;
                    System.out.println(end);
                    int Hours = (int) p.duration / 60;
                    int Minutes = (int) p.duration % 60;
                    System.out.println(Hours + ":" + Minutes);
                    Thread input = new Thread(() -> {
                        synchronized (scannerLock) {
                            p.p.set(sc.nextInt());
                        }
                    });
                    input.setDaemon(true);
                    input.start();
                    try {
                        input.join(60000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (input.isAlive()) {
                        input.interrupt();
                    }
                    if (p.p.get() == 2) {
                        System.out.println("Finally => "+Hours + ":" + Minutes);
                        break;
                    }else {System.out.println("input 2 to end");}
                }
            }
            else if (interaction ==2) {
                return;
            } else {System.out.println("input 1 to start");}
        }

    }
    public static void TodayAllStadyTime() {

    }

}
