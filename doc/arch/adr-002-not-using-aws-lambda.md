# Decision record: Not using AWS Lambda #

## Date ##

04/01/2016

## Context ##

We have two core applications in register product i.e. mint and read api. 

Mint application holds the logic of appending new entries in to the append only 
entry store of a register. currently we are using postgres database as append only entry store.

Read api is a web app which uses the postgres database backend. Application reads the data from database 
and presents in pretty formats like json, yaml, csv, tsv, turtle and html where html view provides 
linking between registers. Postgres db can be recreated at any point of time because it is an indexed 
view of append only mint database. The new entries in this database are replicated from mint database when available. 
           
The replication from mint to read api database is done by a separate application i.e. indexer. 
Indexer reads the mint db periodically and whenever the new entries are available it copies them 
to the read api database. Currently we have only one indexer application deployed on a dedicated 
EC2 instance. One indexer deployment can replicate data for multiple registers which is driven by 
configuration. 

The points to consider in above approach are below:

   - Indexer is pull based application which keeps on polling mint database.
   - Indexer has single point of failure, if the application is down then the whole data copying process will stops. 
   - There can not be two or more parallel indexer instance for a register (not sure that it is possible efficiently). 
   - Managing the separate code deploy process to deploy changes on ec2 instance.
   - Management of a separate dedicated ec2 instance.

Considering the points above we thought aws lambda can provide a better solution given our deployment 
infrastructure is in AWS. We played a story to learn more about lambda with in the context. Below is 
the learning in compare to the points above:
   
   - Lambda functions can poll database or execute in response of an event.
   - We can write a lambda function per register which solves the single point of failure issue but then 
        managing many functions can be hard.
   - Lambda is compute service so no need to think about management of resources to execute the code.
   - If we choose to write a scheduled function there is a limit of only 50 functions per account. there are some 
        more limits which are explained in [http://docs.aws.amazon.com/lambda/latest/dg/limits.html]
        (http://docs.aws.amazon.com/lambda/latest/dg/limits.html). 
   - We currently use AWS code-deploy to deploy the indexer application. There is no integration from code-deploy to directly 
        deploy the code to lambda. we have to write rules to deploy code from s3 and then keep 
        track of currently deployed code artifact and also requires more work to rollback when required.       
   - Our databases are on RDS and to accessing RDS from lambda is not easy. We need to provide public ip to 
       the RDS database to access it from lambda, Read [https://forums.aws.amazon.com/thread.jspa?threadID=166946]
       (https://forums.aws.amazon.com/thread.jspa?threadID=166946). Apart from this currently there is no connection pooling support 
       [https://forums.aws.amazon.com/thread.jspa?threadID=216000](https://forums.aws.amazon.com/thread.jspa?threadID=216000).
   - Cost might be high in compare to ec2 instance because pricing of lambda function is based on number 
        of requests and the compute duration of function.
   - In near future we will use Certificate transparency [https://www.certificate-transparency.org/]
        (https://www.certificate-transparency.org/) as append only data store which will replace the 
        postgres database. CT people are also working on ct log mirroring which will provide log replication, We might choose to use it.  
  
## Decision ##

After comparing the lambda we realize that the lambda might not be the right solution for us. There are multiple 
issues like RDS access in compare to current deployment solution. In any case we have to manage the application, 
code pipeline and support the infrastructure, The only thing it can reduce ec2 instance which is in comparision not that bad.    

## Status ##

waiting

## Consequences ##

- There is a need to tweak the indexer deployment when there are many many registers and 
    one indexer application can not handle all of them.  

## Reference ##

[http://aws.amazon.com/lambda/](http://aws.amazon.com/lambda/)
[http://docs.aws.amazon.com/lambda/latest/dg/welcome.html](http://docs.aws.amazon.com/lambda/latest/dg/welcome.html)
