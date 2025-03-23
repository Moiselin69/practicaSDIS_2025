@echo off
REM Lanzar Cliente Unitario Windows
cd ..
echo Se lanzará el cliente unitario Auth
java -cp "out;libs/*" sdis.broker.client.util.Auth

echo Se lanzará el cliente unitario AddMsg
java -cp "out;libs/*" sdis.broker.client.util.AddMsg

echo Se lanzará el cliente unitario ReadQ"
java -cp "out;libs/*" sdis.broker.client.util.ReadQ

echo Se lanzará el cliente unitario DeleteQ
java -cp "out;libs/*" sdis.broker.client.util.DeleteQ

echo Se lanzará el cliente unitario State
java -cp "out;libs/*" sdis.broker.client.util.State