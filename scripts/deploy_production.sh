#!/usr/bin/env bash

ANSIBLE_SSH_PIPELINING=1 ANSIBLE_COLOR_VERBOSE=white ansible-playbook -l production -i ansible/hosts ansible/site.yml \
--vault-password-file /etc/ansible-vault-password
