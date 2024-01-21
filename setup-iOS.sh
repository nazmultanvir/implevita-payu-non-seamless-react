#!/bin/bash

# This script will do following things:
# 1. install node module
# 2. install pods
# 3. open example.xcworkspace

cd example &&
npm install &&
npm install .. &&
react-native link &&
cd ios &&
pod install &&
open example.xcworkspace
