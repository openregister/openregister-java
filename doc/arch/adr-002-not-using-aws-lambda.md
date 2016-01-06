# Decision record: Not using AWS Lambda #

## Date ##

04/01/2016

## Context ##

We have two core applications in register product: the [mint][] and the [read api][].

The mint holds the logic of adding new entries to the append-only
entry store of a register.  Currently we are using [postgres][] as the
storage engine for the append only entry store.

The read api is a web app which uses a (separate) postgres backend.
It reads data from the database and presents it in pretty formats like
json, yaml, csv, tsv, turtle and html.  For formats that support
linking, the read api provides linking between registers.  The
postgres db can be recreated at any point in time because it is an
indexed view of the append-only mint database.  The new entries in the
mint database are replicated to the read api database when available.
           
The replication from the mint database to the read api database is
done by a separate application: the [indexer][].  The indexer reads
the mint db periodically, and whenever the new entries are available
it copies them to the read api database.  Currently we have only one
indexer application deployed on a dedicated EC2 instance.  One indexer
deployment can replicate data for multiple registers, driven by
configuration.

The points to consider in above approach are below:

   - The indexer is pull-based, ie it works by polling the mint database.
   - The indexer is a single point of failure: if the application
     fails then the whole data copying process will stop.
   - There can not be two or more parallel indexing processes for a
     given register (although it's not clear if it is even possible to
     do this efficiently).
   - A separate app means a separate CodeDeploy process to deploy the
     code for the indexer.
   - The indexer requires a separate EC2 instance, along with all of
     the management that entails.

Considering the points above we thought [AWS Lambda][] could provide a
better solution, given we are already using AWS. We played a spike to
learn more about Lambda within this context. This is what we learned:
   
   - Lambda functions can poll a database or execute in response to an event.
   - We can write a Lambda function per register which solves the
        single point of failure issue; but then managing many
        functions can be hard.
   - Lambda is a compute service so no need to think about management
     of resources to execute the code.
   - If we choose to write a scheduled function there is a limit of
        only 50 functions per account.  There are
        [some more limits](http://docs.aws.amazon.com/lambda/latest/dg/limits.html)
        too.
   - We currently use AWS CodeDeploy to deploy the indexer
        application. There is no integration from CodeDeploy to
        directly deploy the code to lambda.  We would have to write
        rules to deploy code from S3 and then keep track of currently
        deployed code artifact; we would also have to consider how to
        rollback when required.

   - Our databases are on RDS and to accessing RDS from lambda is not
       easy.  The current way to do it is
       [to provide a public IP to the RDS database to access it from lambda](https://forums.aws.amazon.com/thread.jspa?threadID=166946).
       [re:Invent 2015 announced support for accessing VPC resources from lambda](https://aws.amazon.com/blogs/aws/aws-lambda-update-python-vpc-increased-function-duration-scheduling-and-more/)
       but this does't seem to have actually landed yet.  In addition,
       [currently there is no connection pooling support](https://forums.aws.amazon.com/thread.jspa?threadID=216000).

   - Cost might be high in compare to ec2 instance because pricing of
        lambda function is based on number of requests and the compute
        duration of function.
   - In near future we will use Certificate transparency
        [https://www.certificate-transparency.org/]
        (https://www.certificate-transparency.org/) as append only
        data store which will replace the postgres database. CT people
        are also working on ct log mirroring which will provide log
        replication, We might choose to use it.

## Decision ##

We will not use AWS Lambda for indexing the mint database.

## Status ##

Accepted.

## Consequences ##

There will be a need to tweak the indexer deployment when there are
many many registers and one indexer application can not handle all of
them.

## Reference ##

[http://aws.amazon.com/lambda/](http://aws.amazon.com/lambda/)
[http://docs.aws.amazon.com/lambda/latest/dg/welcome.html](http://docs.aws.amazon.com/lambda/latest/dg/welcome.html)

[AWS Lambda]: http://docs.aws.amazon.com/lambda/latest/dg/welcome.html
[indexer]: https://github.com/openregister/indexer
[mint]: https://github.com/openregister/mint
[postgres]: http://www.postgresql.org/
[read api]: https://github.com/openregister/presentation

