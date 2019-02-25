#!/usr/bin/env bash

    vbase=$( date +v%Y%m%d )
    echo release
    release=1
    count=1
    while [ $count -ne "0" ]; do
      count=`git tag | grep $vbase-$release | wc -l`
      if [ $count -ne "0" ]; then
        release=$(( $release + 1 ))
        echo new release $release
      fi
    done
    echo Tag $version