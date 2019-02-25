#!/usr/bin/env bash

    vbase=$( date +v%Y%m%d )
    echo release
    release=1
    count=1
    while true; do
      count=`git tag | grep $vbase-$release | wc -l` || "1"
      if [ $count -ne "0" ]
       then release=$(( $release + 1 ))
      else
        break
      fi
      echo ""
    done
    echo Tag $vbase-$release