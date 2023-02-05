#!/usr/bin/env bash

ANSIBLE_SSH_PIPELINING=1 ANSIBLE_COLOR_VERBOSE=white ansible-playbook -l staging -i ansible/hosts ansible/site.yml
