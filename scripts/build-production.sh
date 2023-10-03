#!/bin/bash

rm -r .shadow-cljs
rm -r resources/public/js

shadow-cljs release production

clj -T:build uber