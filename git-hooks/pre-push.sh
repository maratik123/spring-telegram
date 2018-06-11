#!/bin/sh
# this hook is in SCM so that it can be shared
# to install it, create a symbolic link in the projects .git/hooks folder
#
#       i.e. - from the .git/hooks directory, run
#               $ ln -s ../../git-hooks/pre-push.sh pre-push

# stash any unstaged changes
git stash -q --keep-index

# run the checks with the gradle wrapper
./gradlew check
# Another possibility could be to run an aggregate test report task and automatically open the generated html

# store the last exit code in a variable
RESULT=$?

# unstash the unstashed changes
git stash pop -q

# return the './gradlew check' exit code
exit $RESULT
