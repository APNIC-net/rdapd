#!/bin/sh
# NOTE: Be aware that the combination of source filtering and bash variables
#       may cause unintended interpolation.

# Add Vault CA to Java keystore.
VAULT_CACERT=${VAULT_CACERT:-/etc/vault/ca.crt}
if [ -r "$VAULT_CACERT" ]; then
    /usr/bin/keytool -noprompt -import -trustcacerts \
        -file "$VAULT_CACERT" -alias VaultCA \
        -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit
else
    echo 'WARNING: unable to read Vault CA certificate' >&2
fi

# Put Vault user id into environment.

# TODO: Later versions of Kubernetes will allow us to inject this directly
#       into the environment of the process.
#       When it is able to do so we need to look at loading the CA from
#       within the java code so we no longer need an entrypoint script.

if [ -r /etc/vault-user/user-id ]; then
    # Note the environment variable must be a 'PASSWORD' variable
    # otherwise /env will show it's value.
    export VAULT_USER_PASSWORD=$(cat /etc/vault-user/user-id)
else
    echo 'Unable to read Vault user id' >&2
fi

echo "Launching with arguments: $@"

exec java "$\@" -jar @project.artifactId@-@project.version@.@project.packaging@
