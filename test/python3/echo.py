# -*- coding: UTF-8 -*-

from erlport import Atom
from erlport import erlang

def echo(r):
    return r

def rev_call(pid, r):
    print(f'pid: {pid}')
    erlang.call(Atom(b'erlport_SUITE'), Atom(b'handle_call'), [pid, r])
    return
