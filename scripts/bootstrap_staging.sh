#!/usr/bin/env bash

ansible-playbook -l staging -i ansible/hosts ansible/bootstrap.yml
