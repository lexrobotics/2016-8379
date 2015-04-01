/* Copyright (c) 2014 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

/**
 * TeleOp Mode
 * <p>
 *Enables control of the robot via the gamepad
 */
public class TeleOp extends OpMode {

  // position of the claw servo
  double clawPosition;

  // amount to change the claw servo position by
  double clawDelta = 0.01;

  // position of the wrist servo
  double wristPosition;

  // amount to change the wrist servo position by
  double wristDelta = 0.01;

  DcMotor motorRight;
  DcMotor motorLeft;
  DcMotor flag;
  DcMotor arm;

  Servo claw;
  Servo wrist;

  /**
   * Constructor
   */
  public TeleOp() {

  }

  /*
   * Code to run when the op mode is first enabled goes here
   * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#start()
   */
  @Override
  public void start() {

    motorRight = hardwareMap.dcMotor.get("right");
    motorLeft = hardwareMap.dcMotor.get("left");
    flag = hardwareMap.dcMotor.get("flag");
    arm = hardwareMap.dcMotor.get("arm");
    claw = hardwareMap.servo.get("a");
    wrist = hardwareMap.servo.get("b");

    motorRight.setDirection(DcMotor.Direction.REVERSE);

    // set the starting position of the wrist and claw
    wristPosition = 0.5;
    clawPosition = 0.5;
  }

  /*
   * This method will be called repeatedly in a loop
   * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#run()
   */
  @Override
  public void run() {

    /*
     * Gamepad 1
     *
     * Gamepad 1 controls the motors via the left stick, and it controls the wrist/claw via the a,b,
     * x, y buttons
     */

    // throttle:  left_stick_y ranges from -1 to 1, where -1 is full up,  and 1 is full down
    // direction: left_stick_x ranges from -1 to 1, where -1 is full left and 1 is full right
    float throttle  = -gamepad1.left_stick_y;
    float direction =  gamepad1.left_stick_x;
    float right = throttle - direction;
    float left  = throttle + direction;

    // clip the right/left values so that the values never exceed +/- 1
    right = Range.clip(right, -1, 1);
    left  = Range.clip(left,  -1, 1);

    // write the values to the motors
    motorRight.setPower(right);
    motorLeft.setPower(left);

    // update the position of the wrist
    if (gamepad1.a) {
      wristPosition -= wristDelta;
    }

    if (gamepad1.y) {
      wristPosition += wristDelta;
    }

    // update the position of the claw
    if (gamepad1.x) {
      clawPosition -= clawDelta;
    }

    if (gamepad1.b) {
      clawPosition += clawDelta;
    }

    if (gamepad1.left_bumper) {
      flag.setPower(0.3);
    } else {
      flag.setPower(0.0);
    }

    if (gamepad1.dpad_up) {
      arm.setPower(0.2);
    } else if (gamepad1.dpad_down) {
      arm.setPower(-0.05);
    } else {
      arm.setPower(0.0);
    }

    // clip the position values so that they never exceed 0..1
    wristPosition = Range.clip(wristPosition, 0, 1);
    clawPosition = Range.clip(clawPosition, 0, 1);

    // write position values to the wrist and claw servo
    wrist.setPosition(wristPosition);
    claw.setPosition(clawPosition);

    /*
     * Gamepad 2
     *
     * Gamepad controls the motors via the right trigger as a throttle, left trigger as reverse, and
     * the left stick for direction. This type of control is sometimes referred to as race car mode.
     */

    // we only want to process gamepad2 if someone is using one of it's analog inputs. If you always
    // want to process gamepad2, remove this check
    if (gamepad2.atRest() == false) {

      // throttle is taken directly from the right trigger, the right trigger ranges in values from
      // 0 to 1
      throttle = gamepad2.right_trigger;

      // if the left trigger is pressed, go in reverse
      if (gamepad2.left_trigger != 0.0) {
        throttle = -gamepad2.left_trigger;
      }

      // assign throttle to the left and right motors
      right = throttle;
      left = throttle;

      // now we need to apply steering (direction). The left stick ranges from -1 to 1. If it is
      // negative we want to slow down the left motor. If it is positive we want to slow down the
      // right motor.
      if (gamepad2.left_stick_x < 0) {
        // negative value, stick is pulled to the left
        left = left * (1 + gamepad2.left_stick_x);
      }
      if (gamepad2.left_stick_x > 0) {
        // positive value, stick is pulled to the right
        right = right * (1 - gamepad2.left_stick_x);
      }

      // write the values to the motor. This will over write any values placed while processing gamepad1
      motorRight.setPower(right);
      motorLeft.setPower(left);
    }

    telemetry.addData("Text", "free flow text");
    telemetry.addData(" left motor", motorLeft.getPower());
    telemetry.addData("right motor", motorRight.getPower());
  }

  /*
   * Code to run when the op mode is first disabled goes here
   * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#stop()
   */
  @Override
  public void stop() {

  }

}
