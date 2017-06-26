set timeout 10

spawn ssh-add /tmp/deploy_rsa
expect "Enter passphrase for /tmp/deploy_rsa:"
send "mamakami\n"
