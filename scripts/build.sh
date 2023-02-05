#!/bin/bash

rm -r .shadow-cljs
rm -r resources/public/js

shadow-cljs release dev

clj -T:build uber