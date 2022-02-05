//package us.brainstormz.rataTony;
//
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//
//import us.brainstormz.pid.PID;
//
//public class tesfds extends LinearOpMode {
//
//    @Override
//    public void runOpMode() throws InterruptedException {
//        waitForStart();
//        int target = 0;
//
//        new Thread() {
//            final PID armPID = new PID(0.0, 0.0, 0.0, 0.0);
//
//            @Override
//            public void start() {
//                while (opModeIsActive()) {
//                    armPID.calcPID(target, v4b.currentpos());
//                    if (isStopRequested()) {
//                        this.stop();
//                    }
//                }
//            }
//        }.start();
//
//    }
//
//}
