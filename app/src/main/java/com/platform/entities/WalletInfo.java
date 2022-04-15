package com.platform.entities; 

public class WalletInfo {

    // Not certain this is necessary as it is used for the KeyStores
    // Which are not able to call and not part of the Litewallet architecture
    // To Be Removed
    /**WalletInfo Mock:

     Key: “wallet-info”

     {
        “classVersion”: 2, //used for versioning the schema
        “creationDate”: 123475859, //Unix timestamp
        “name”: “My Litecoin”,
        “currentCurrency”: “USD”
     }
     */

    public int classVersion;
    public int creationDate;
    public String name;
}
