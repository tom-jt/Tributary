## Simple message lifecycle

> cluster setup

create topic top Integer

create partition top par

> producer setup

create producer pro Integer Random

> consumer group setup

create consumer group gro top Range

create consumer gro con

show consumer group gro

> produce event

produce event pro top testInt msg1

<!-- note: different CLI command for produce -->
<!-- note JSON file testInt (and testStr for later) -->

show topic top

> consume event

consume event con par

show topic top

> consume empty

consume event con par

## Multiple events types, topics, and partitions

> cluster setup

create topic top Integer

create topic top2 String

create partition top par

create partition top par2

create partition top2 par21

create partition top2 par22

> producer setup

create producer pro Integer Random

create producer pro2 String Random

> consumer group setup

create consumer group gro top Range

create consumer group gro2 top2 Range

create consumer gro con

create consumer gro2 con2

show consumer group gro

show consumer group gro2

> produce events for top

produce event pro top testInt int1

produce event pro top testInt int2

produce event pro top testInt int3

show topic top

> consume events for top

consume events con par 3

consume events con par2 3

show topic top

> produce events for top2

produce event pro2 top2 testStr str1

produce event pro2 top2 testStr str2

produce event pro2 top2 testStr str3

show topic top2

> consume events for top2

consume events con2 par21 3

consume events con2 par22 3

show topic top2

## Producer allocation - random

> cluster setup

create topic top Integer

create partition top par

create partition top par2

create partition top par3

create partition top par4

create partition top par5

create partition top par6

create partition top par7

> producer setup

create producer pro Integer Random

> produce events

produce event pro top testInt msg1

produce event pro top testInt msg2

produce event pro top testInt msg3

show topic top

## Producer allocation - manual

> cluster setup

create topic top Integer

create partition top par

create partition top par2

create partition top par3

create partition top par4

create partition top par5

create partition top par6

create partition top par7

> producer setup

create producer pro Integer Manual

> produce events

produce event pro top testInt msg1

show topic top

<!-- expected par2 has msg1 -->

produce event pro top testInt msg2 par

produce event pro top testInt msg3 par7

produce event pro top testInt msg4 par3

produce event pro top testInt msg5 par6

show topic top

<!-- expected par has msg2, par2 has msg1, par3 has msg4, par6 has msg5, par7 has msg3 -->

## Rebalancing - Range

> cluster setup

create topic top Integer

create partition top par

create partition top par2

> consumer group setup

create consumer group gro top Range

create consumer gro con

show consumer group gro

> create consumers

create consumer gro con2

show consumer group gro

create consumer gro con3

show consumer group gro

> create partitions

create partition top par3

create partition top par4

create partition top par5

create partition top par6

create partition top par7

show consumer group gro

> delete consumer

delete consumer con2

show consumer group gro

## Rebalancing - RoundRobin

> cluster setup

create topic top Integer

create partition top par

create partition top par2

> consumer group setup

create consumer group gro top RoundRobin

create consumer gro con

show consumer group gro

> create consumers

create consumer gro con2

show consumer group gro

create consumer gro con3

show consumer group gro

> create partitions

create partition top par3

create partition top par4

create partition top par5

create partition top par6

create partition top par7

show consumer group gro

> delete consumer

delete consumer con2

show consumer group gro

## Parallel consume

> cluster setup

create topic top Integer

create partition top par

> consumer group setup

create consumer group gro top Range

create consumer group gro2 top Range

create consumer gro con

create consumer gro2 con2

show consumer group gro

show consumer group gro2

> producer setup

create producer pro Integer Random

> produce event

produce event pro top testInt msg1

produce event pro top testInt msg2

produce event pro top testInt msg3

produce event pro top testInt msg4

produce event pro top testInt msg5

produce event pro top testInt msg6

produce event pro top testInt msg7

produce event pro top testInt msg8

produce event pro top testInt msg9

produce event pro top testInt msg10

show topic top

> parallel consume

parallel consume (con, par), (con2, par), (con, par), (con2, par), (con, par), (con2, par), (con, par), (con2, par), (con, par), (con2, par)

show topic top

## Parallel produce

> cluster setup

create topic top Integer

create partition top par

> producer setup

create producer pro Integer Random

> parallel produce

parallel produce (pro, top, testInt, msg1), (pro, top, testInt, msg2), (pro, top, testInt, msg3), (pro, top, testInt, msg4), (pro, top, testInt, msg5), (pro, top, testInt, msg6), (pro, top, testInt, msg7), (pro, top, testInt, msg8), (pro, top, testInt, msg9), (pro, top, testInt, msg10)

show topic top

## Multiple consumer groups, replay

> cluster setup

create topic top Integer

create partition top par

create partition top par2

> consumer group setup

create consumer group gro top Range

create consumer group gro2 top Range

create consumer group gro3 top Range

create consumer gro con

create consumer gro2 con2

create consumer gro3 con3

show consumer group gro

show consumer group gro2

show consumer group gro3

> producer setup

create producer pro Integer Manual

> produce event

produce event pro top testInt 1-msg1 par

produce event pro top testInt 1-msg2 par

produce event pro top testInt 1-msg3 par

produce event pro top testInt 1-msg4 par

produce event pro top testInt 1-msg5 par

produce event pro top testInt 1-msg6 par

produce event pro top testInt 1-msg7 par

produce event pro top testInt 1-msg8 par

produce event pro top testInt 1-msg9 par

produce event pro top testInt 1-msg10 par

show topic top

produce event pro top testInt 2-msg1 par2

produce event pro top testInt 2-msg2 par2

produce event pro top testInt 2-msg3 par2

produce event pro top testInt 2-msg4 par2

produce event pro top testInt 2-msg5 par2

produce event pro top testInt 2-msg6 par2

produce event pro top testInt 2-msg7 par2

produce event pro top testInt 2-msg8 par2

produce event pro top testInt 2-msg9 par2

produce event pro top testInt 2-msg10 par2

show topic top

> consumer events

parallel consume (con, par), (con2, par2), (con3, par), (con, par2), (con2, par), (con3, par2), (con, par), (con2, par2), (con3, par), (con, par2), (con2, par), (con3, par2), (con, par), (con2, par2), (con3, par), (con, par2), (con2, par), (con3, par2), (con, par), (con2, par2), (con3, par), (con, par2), (con2, par), (con3, par2)

<!-- 12 consumes from par, 12 consumes from par2, 8 consumes for each consumer -->

<!-- 24 total consumptions, 20 messages total, so 4 consumptions should fail -->

show topic top

> replay events - check all consumers

playback con par 1

<!-- should only have 1-msg#, and not have any 2-msg# -->

playback con par2 1

<!-- should only have 2-msg#, and not have any 1-msg# -->

playback con2 par 1

playback con2 par2 1

playback con3 par 1

playback con3 par2 1

> replay events - offset test

playback con par 5

playback con par 10

playback con par 100
