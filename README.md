ActorPool
=

Introduction
-
ActorPool is a simple implementation of a [Myriad](https://emphysic.com/myriad/) LinkedWorkerPool, designed to run on a server.  The idea is that a remote system can be configured to start an ActorPool and be able to provide additional processing power when needed.

If you're using the [Myriad Desktop](https://gitlab.com/ccoughlin/MyriadDesktop) tool, just update the path in the stage to the ActorPool's path, and make sure the "Remote" check box is checked so Desktop knows this stage will be handled by an external process.  That's it!