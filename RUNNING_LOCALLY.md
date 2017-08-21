# Create your own register locally

You can use this script to run your own register locally and experiment with its configuration.

Please note that this script is experimental and has not been fully tested. The outputs and functionality are likely to change as the registers team learns more about the needs of users creating their own registers.

If you'd like to experiment with creating your own register or would like to be involved in user research, please [contact the GDS registers team](https://registers.cloudapps.digital/support.html).

## Before you start

You'll need:
* Docker
* Python3
* curl
* An understanding of the [register APIs](https://registers-docs.cloudapps.digital/)

This iteration of the script is pre-configured to spin up a register of countries based on the [Country register](https://country.register.gov.uk/), which is currently in beta.

It also uses "basic registers" such as the [register register](https://register.register.gov.uk/), [field register](https://field.register.gov.uk/).

You can amend these details once you have a copy of the Country and Territory registers running locally. You can find credential information in the configuration files.

## Run register locally

Run `./run-application.sh` to spin up a local copy of `openregister-java` using Docker.

The command will create Docker containers that will:
* build the application from source
* start and configure the database
* run the "basic" registers, such as the Register Register and Field Register with `config.register.basic.yaml`
* run a register configured with `config.docker.register.yaml`

The basic registers will be cloned from a specific phase, such as beta. You can see them locally at `*.local.openregister.org:8081`. For example, `field.local.openregister.org:8081`.

You should now see the Country register locally at `127.0.0.1:8080`.

If you want to amend this register, you can:
* create a new register
* change the configuration

## Clone a different register

The `./run-application.sh` script is configured to spin up a clone of the [Country beta register](https://country.register.gov.uk/). To clone a different register from another phase, you need to change some configuration.

For example, you might want to clone the [School type register](https://school-type.alpha.openregister.org/) from the alpha phase.

Go to the `config.docker.register.yaml` file.

Change `register: country` to `register: school-type`.

Update the schema in the same way, to `schema: school-type`.

Now re-run the application using `ENVIRONMENT=alpha REGISTERS=school-type ./run-application.sh`. You should now see the School type register locally at `127.0.0.1:8080`.

## Create a new register

To create a new register, you first need to create a new entry in the register register.

Choose a primary key for your register. This will also be the name of your register. For example, foobar.

Add the name to the field register using:
`$ echo '[{"field":"foobar", "datatype": "string", "phase": "alpha", "cardinality": "1", "text": "test field"}]' | python3 ./scripts/json-to-rsf/json2rsf.py user field | curl field.local.openregister.org:8081/load-rsf -H "Content-Type: application/uk-gov-rsf" --data-binary @- -u foo:bar`

Then, you'll need to create an entry in the register register using:
`$ echo '[{"phase":"alpha","registry":"government-digital-service","text":"A test register","fields":["foobar","name","start-date","end-date"],"register":"foobar"}]' | python3 ./scripts/json-to-rsf/json2rsf.py user register | curl register.local.openregister.org:8081/load-rsf -H "Content-Type: application/uk-gov-rsf" --data-binary @- -u foo:bar`

Check this is successful at `field.local.openregister.org:8081/records` and `register.local.openregister.org:8081/records`. You should see the name of your register, such as `foobar`, as the first record. 

Now you can update your local register.

## Change the configuration

First, you'll need to change the configuration.

Go to the `config.docker.register.yaml` file.

Change `register: country` to the name of your register, for example `register: foobar`.

Update the schema in the same way, for example: `schema: foobar`.

Restart your docker container:
`docker restart openregister-register`

You should see your new undefined register at `127.0.0.1:8080`.

## Load data into your register

You can now load your own data in the new register.

First, you need to populate the register with its register definition and field definitions. These should be the same as those listed in the field and register registers.

```
$ echo '[{"name":"foobar"}]' | python3 ./scripts/json-to-rsf/json2rsf.py system name | curl 127.0.0.1:8080/load-rsf -H "Content-Type: application/uk-gov-rsf" --data-binary @- -u foo:bar
$ echo '[{"field":"foobar", "datatype": "string", "phase": "alpha", "cardinality": "1", "text": "test field"}]' | python3 ./scripts/json-to-rsf/json2rsf.py system field:foobar | curl 127.0.0.1:8080/load-rsf -H "Content-Type: application/uk-gov-rsf" --data-binary @- -u foo:bar
$ echo '[{"field":"name", "datatype": "string", "phase": "beta", "cardinality": "1", "text": "The commonly-used name of a record"}]' | python3 ./scripts/json-to-rsf/json2rsf.py system field:name | curl 127.0.0.1:8080/load-rsf -H "Content-Type: application/uk-gov-rsf" --data-binary @- -u foo:bar
$ echo '[{"field":"start-date", "datatype": "datetime", "phase": "beta", "cardinality": "1", "text": "The date a record first became relevant to a register."}]' | python3 ./scripts/json-to-rsf/json2rsf.py system field:start-date | curl 127.0.0.1:8080/load-rsf -H "Content-Type: application/uk-gov-rsf" --data-binary @- -u foo:bar
$ echo '[{"field":"end-date", "datatype": "datetime", "phase": "beta", "cardinality": "1", "text": "The date a record stopped being applicable."}]' | python3 ./scripts/json-to-rsf/json2rsf.py system field:end-date | curl 127.0.0.1:8080/load-rsf -H "Content-Type: application/uk-gov-rsf" --data-binary @- -u foo:bar
$ echo '[{"phase":"alpha","registry":"government-digital-service","text":"A test register","fields":["foobar","name","start-date","end-date"],"register":"foobar"}]' | python3 ./scripts/json-to-rsf/json2rsf.py system register:foobar | curl 127.0.0.1:8080/load-rsf -H "Content-Type: application/uk-gov-rsf" --data-binary @- -u foo:bar
```

You can now load foobar data.

For example:

```
$ echo '[{"foobar": "a", "name": "something"}]' | python3 ./scripts/json-to-rsf/json2rsf.py user foobar | curl 127.0.0.1:8080/load-rsf -H "Content-Type: application/uk-gov-rsf" --data-binary @- -u foo:bar
$ echo '[{"foobar": "b", "name": "something else"}]' | python3 ./scripts/json-to-rsf/json2rsf.py user foobar | curl 127.0.0.1:8080/load-rsf -H "Content-Type: application/uk-gov-rsf" --data-binary @- -u foo:bar
$ echo '[{"foobar": "c", "name": "another thing"}]' | python3 ./scripts/json-to-rsf/json2rsf.py user foobar | curl 127.0.0.1:8080/load-rsf -H "Content-Type: application/uk-gov-rsf" --data-binary @- -u foo:bar
```

## Contact and support

The GDS registers team provides operational support from 09:00 - 17:00 Monday-Friday.

[Contact the team](https://registers.cloudapps.digital/support.html) if you have any problems or questions that are not covered in this guide.
