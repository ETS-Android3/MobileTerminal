package edi.md.mobile.Utils;

public class AssortimentEntry {

    private String mGUID;
    String getID(){return mGUID;}
    void setID(String guid){mGUID = guid;}

    private double mCount;
    double getCount(){ return mCount;}
    void setCount( double count){mCount = count;}

    private String mName;
    String getName (){return mName;}
    void setName(String name){mName = name;}

    private double mInvoiceIncomePrice;
    double getInvoiceIncomePrice (){return mInvoiceIncomePrice;}
    void setInvoiceIncomePrice(double incomePrice){mInvoiceIncomePrice = incomePrice;}

    private double mSum;
    double getSum (){return mSum;}
    void setSum(double sum){mSum = sum;}

    private double mInvoiceSalesPrice;
    double getInvoiceSalesPrice (){return mInvoiceSalesPrice;}
    void setInvoiceSalesPrice(double salesPrice){mInvoiceSalesPrice = salesPrice;}


}
