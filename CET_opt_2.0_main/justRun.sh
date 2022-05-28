if [ ! -f out/src/Main.class ]; then
    echo "The Main class file does not exit!"
else
  cd out
  java src.Main
fi