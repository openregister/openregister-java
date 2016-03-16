group ItemDAO;

existingItemHex(listOfHexValues) ::= <<
  select sha256hex from item where sha256hex in (<listOfHexValues>)
>>
