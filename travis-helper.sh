set timeout 10

spawn ssh-add /tmp/rsa_key
expect "Enter passphrase for /tmp/deploy_rsa:"
send "mamakami\n"
