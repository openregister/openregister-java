# Local authorities by start-date example

This example initially adds entries to the register using local-authority-type as the key (this is not included as part of the item definitions).

There are three distinct items added by the five entries:
    - Notts - sha-256:768ccbd7702169778bcc43abe57cd834b977fdef887567c637b296a6e379b4e3, start-date: 1990
    - London - sha-256:f2ad63acbcff98050b0ba1fe708f33711dee1cce8931ba876f0cf2ad6d0225a0, start-date: 1880
    - Leics - sha-256:3300687af84cc4c365407484f93848695a0d82b0918c894e56d2d49ab93ba27c, start-date: 1770

The index 'by start date' will index the entries appended to the register by their `start-date` property.

The following register entries:

append-entry	user	MD	2016-04-05T13:23:05Z	sha-256:768ccbd7702169778bcc43abe57cd834b977fdef887567c637b296a6e379b4e3
append-entry	user	MD	2016-04-05T13:23:05Z	sha-256:768ccbd7702169778bcc43abe57cd834b977fdef887567c637b296a6e379b4e3,sha-256:f2ad63acbcff98050b0ba1fe708f33711dee1cce8931ba876f0cf2ad6d0225a0
append-entry	user	CTY	2016-04-05T13:23:05Z	sha-256:3300687af84cc4c365407484f93848695a0d82b0918c894e56d2d49ab93ba27c
append-entry	user	CTY	2016-04-05T13:23:05Z	sha-256:3300687af84cc4c365407484f93848695a0d82b0918c894e56d2d49ab93ba27c,sha-256:f2ad63acbcff98050b0ba1fe708f33711dee1cce8931ba876f0cf2ad6d0225a0
append-entry	user	MD	2016-04-05T13:23:05Z	sha-256:768ccbd7702169778bcc43abe57cd834b977fdef887567c637b296a6e379b4e3

Will create the following index RSF:

add-item	{"name":"Notts","start-date":"1990"}
add-item	{"name":"London","start-date":"1880"}
add-item	{"name":"Leics","start-date":"1770"}
append-entry	user	1990	2016-04-05T13:23:05Z	sha-256:768ccbd7702169778bcc43abe57cd834b977fdef887567c637b296a6e379b4e3
append-entry	user	1880	2016-04-05T13:23:05Z	sha-256:f2ad63acbcff98050b0ba1fe708f33711dee1cce8931ba876f0cf2ad6d0225a0
append-entry	user	1770	2016-04-05T13:23:05Z	sha-256:3300687af84cc4c365407484f93848695a0d82b0918c894e56d2d49ab93ba27c

There is a special case here, which is that when register entry number 4 is appended with key `CTY` and includes the hash for item of `lndn`,
an additional entry for key `1880` (the start-date of `lndn`) isn't added again to the index RSF. The reason for this is because
nothing has changed in terms of the index `lndn` has been added to the register against a different key but for the key
in the index stays the same. Instead, we use the index table to keep track of this by adding a new row to it with the key `1880`
- more details below.

## Index table

The corresponding index table will be the following:
name		key		hash																sen		een		sien	eien
start-date	1990	768ccbd7702169778bcc43abe57cd834b977fdef887567c637b296a6e379b4e3	1				1
start-date	1880	f2ad63acbcff98050b0ba1fe708f33711dee1cce8931ba876f0cf2ad6d0225a0	2		5		2		2
start-date	1770	3300687af84cc4c365407484f93848695a0d82b0918c894e56d2d49ab93ba27c	3				3
start-date	1880	f2ad63acbcff98050b0ba1fe708f33711dee1cce8931ba876f0cf2ad6d0225a0	4				2

Note that when register entry number 4 (sen=4) is appended to the register, we assign it the sien of 2 in the index table,
rather than leaving this column null.

The reason for doing this is becomes apparent at register entry number 5, where the first `lndn` item instance against key 1880
and sen of 2 is ended; by using sien of 2 for sen=4, we ensure that this row will be returned from the index table and
eventually closed if and when the index is ended.

At register entry number, we end the first `lndn` item where sen=2 and key=1880. When we end it, we set eign=2, however because
the row in the table with sen=4 is also for the item `lndn`, we don't end the entry in the index.



