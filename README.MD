# Welcome to the RemoteSwitchController Project

In this project we connect a XBox Elite 2 Controller to a Nintendo Switch that can be anywhere in the world.
To accomblish this we wrote a Client and Server to Stream the Controller data from one PC to another one that gives this data to a micocontroller to simulate a Ninendo Switch Pro Controller.

## A Huge Thanks

Special Thanks to [This](https://github.com/dornbirndevelops/nintendo-switch-controller-mcu "This") Project. That saved us so much time. Please check them out and leave them a star for thier work.

## Why

To answer the why in the project my friend has no Nindendo Switch and I wanted to play some local multiplayer games with him.
So we decided to Code this project together to play together on my Nintendo Switch.

## Current Features

+ All button / joystick 1 to 1 connectivity
+ Pressing the start button for 2 secounds presses the Nintendo Pro Controller Home button.

## Planned Features / Future deployments

+ As a mapper for using a keyboard and mouse on the Nintendo Switch
+ Compatibility for other Controllers.
+ A Custom PCB for the controller simulation

## Setup

To set this up grab a Arduino UNO and a USB to Serial Converter. You can pretty much follow the instructions in this Project [Here](https://github.com/dornbirndevelops/nintendo-switch-controller-mcu "Here") but keep in mind that the raspberry pi there is out USB to Serial Converter.

Then just connect to Arduino UNO to your Nintendo Switch and the USB to Serial Converter to your PC where you will run the Server software. The server gets configured by command line parameters.

Install on the other Computer the Client Software.
You can change the settings in the Client Software in the config.json file.

## The assembled prototype for the microcontroller simulateing the Nintendo Switch Pro Controller

![ControllerSimulator](https://github.com/FEinerhand/RemoteSwitchController/blob/main/ControllerSimulator.jpeg?raw=true "ControllerSimulator")


