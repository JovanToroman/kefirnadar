#!/usr/bin/env bash

ansible-playbook -l production -i ansible/hosts ansible/bootstrap.yml
