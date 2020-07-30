%%% Copyright (c) 2020, JianBo He <heeejianbo@gmail.com>
%%% All rights reserved.
%%%
%%% Redistribution and use in source and binary forms, with or without
%%% modification, are permitted provided that the following conditions are met:
%%%
%%%  * Redistributions of source code must retain the above copyright notice,
%%%    this list of conditions and the following disclaimer.
%%%  * Redistributions in binary form must reproduce the above copyright
%%%    notice, this list of conditions and the following disclaimer in the
%%%    documentation and/or other materials provided with the distribution.
%%%  * Neither the name of the copyright holders nor the names of its
%%%    contributors may be used to endorse or promote products derived from
%%%    this software without specific prior written permission.
%%%
%%% THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
%%% AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
%%% IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
%%% ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
%%% LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
%%% CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
%%% SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
%%% INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
%%% CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
%%% ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
%%% POSSIBILITY OF SUCH DAMAGE.

-module(erlport_performance_SUITE).

-compile([export_all, nowarn_export_all]).

-import(erlport_test_utils, [script_path/1]).

-define(LOG(Fmt), io:format(standard_error, Fmt, [])).
-define(LOG(Fmt, Args), io:format(standard_error, Fmt, Args)).

%%--------------------------------------------------------------------
%% Setups
%%--------------------------------------------------------------------

all() ->
    [{group, python3}, {group, java}].

groups() ->
    Cases = [t_concurrency_echo],
    [{python3, Cases}, {java, Cases}].

init_per_suite(Cfg) ->
    application:ensure_all_started(erlport),
    Cfg.

end_per_suite(_) ->
    application:stop(erlport),
    ok.

init_per_group(GrpName = python3, Cfg) ->
    Opts = [{python, atom_to_list(GrpName)},
            {python_path, script_path(GrpName)}],
    {ok, Pid} = python:start(Opts),
    [{pid, Pid}, {mod, 'echo'}, {'fun', echo} | Cfg];

init_per_group(GrpName = java, Cfg) ->
    Opts = [{java, atom_to_list(GrpName)},
            {java_path, script_path(GrpName)}],
    {ok, Pid} = java:start(Opts),
    [{pid, Pid}, {mod, 'Echo'} | Cfg].

end_per_group(_GrpName, Cfg) ->
    Pid = proplists:get_value(pid, Cfg),
    erlport:stop(Pid),
    ok.

%%--------------------------------------------------------------------
%% Cases
%%--------------------------------------------------------------------

matix() ->
    %% Procs count, Req/procs, Req size
    [ {100, 1, 1024}        %% 100KB
    , {100, 100, 1024}      %% 10MB
    , {100, 1000, 1024}     %% 100MB
    , {100, 10000, 1024}    %% 1000MB

    , {100, 100, 32}        %% 312KB
    , {100, 100, 64}        %% 624KB
    , {100, 100, 128}       %% 1.24MB
    , {100, 100, 1024}      %% 100MB
    , {100, 100, 8192}      %% 800MB
    , {100, 100, 65536}     %% 2500MB
    ].

t_concurrency_echo(Cfg) ->
    Pid = proplists:get_value(pid, Cfg),
    Mod = proplists:get_value(mod, Cfg),
    [shot_one_case(M, Pid, Mod) || M <- matix()].

shot_one_case({ProcCnt, ReqCnt, S}, Pid, Mod) ->
    Throughput = ProcCnt * ReqCnt * S,
    RequestCnt = ProcCnt * ReqCnt,
    Bin = chaos_bin(S),
    P = self(),
    ?LOG("\n"),
    ?LOG("=====================================\n"),
    ?LOG("-- Start a concurrency throughput testing 1\n"),
    ?LOG("--  Request: ~s, size: ~s Total: ~s\n", [format_cnt(RequestCnt), format_byte(S), format_byte(Throughput)]),
    ?LOG("-- Runing...\n"),
    statistics(runtime),
    statistics(wall_clock),
    [spawn(fun() ->
        [begin
             {I, J, _} = erlport:call(Pid, Mod, 'echo', [{I, J, Bin}], []),
             P ! {echo, I, J}
         end || J <- lists:seq(1, ReqCnt)]
     end) || I <- lists:seq(1, ProcCnt)],

    Clt = fun _F(0) -> ok;
              _F(X) ->
                  receive
                      {echo, _I, _J} -> _F(X-1)
                  after 1000 -> exit(timeout)
                  end
          end,
    Clt(ProcCnt*ReqCnt),
    {_, Time1} = statistics(runtime),
    {_, Time2} = statistics(wall_clock),
    ?LOG("-- Done\n--\n"),
    ?LOG("-- Consume CPU time: ~s, Procs time: ~s\n", [format_ts(Time1), format_ts(Time2)]),
    ?LOG("--\n"),
    ?LOG("--\n"),
    ?LOG("--        TPS: ~s/s (~s/s) \n", [format_cnt(1000*RequestCnt/Time1), format_cnt(1000*RequestCnt/Time2)]),
    ?LOG("-- Throughput: ~s/s (~s/s) \n", [format_byte(1000*Throughput/Time1), format_byte(1000*Throughput/Time2)]),
    ?LOG("=====================================").

%%--------------------------------------------------------------------
%% Utils

chaos_bin(S) ->
    iolist_to_binary([255 || _ <- lists:seq(1, S)]).

format_ts(Ms) ->
    case Ms > 1000 of
        true ->
            lists:flatten(io_lib:format("~.2fs", [Ms/1000]));
        _ ->
            lists:flatten(io_lib:format("~wms", [Ms]))
            
    end.

format_byte(Byte) ->
    if
        Byte > 1024*124 ->
            lists:flatten(io_lib:format("~.2fMB", [Byte/1024/1024]));
        Byte > 1024 ->
            lists:flatten(io_lib:format("~.2fKB", [Byte/1024]));
        true ->
            lists:flatten(io_lib:format("~wB", [Byte]))
    end.

format_cnt(Cnt) ->
    case Cnt > 1000 of
        true ->
            lists:flatten(io_lib:format("~.2fk", [Cnt / 1000]));
        _ ->
            lists:flatten(io_lib:format("~w", [Cnt]))
    end.
