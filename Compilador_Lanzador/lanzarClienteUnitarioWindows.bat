@echo off
REM Lanzar Cliente Unitario Windows
cd ..
echo Se lanzara el cliente unitario Auth
java -cp "out;libs/*" sdis.broker.client.unit.Auth

echo Se lanzara el cliente unitario AddMsg
java -cp "out;libs/*" sdis.broker.client.unit.AddMsg

echo Se lanzara el cliente unitario ReadQ
java -cp "out;libs/*" sdis.broker.client.unit.ReadQ

echo Se lanzara el cliente unitario DeleteQ
java -cp "out;libs/*" sdis.broker.client.unit.DeleteQ

echo Se lanzara el cliente unitario State
java -cp "out;libs/*" sdis.broker.client.unit.State
