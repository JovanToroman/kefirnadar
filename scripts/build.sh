#!/bin/bash

rm -r .shadow-cljs
rm -r resources/public/js

shadow-cljs release staging

clj -T:build uber