// 
// Decompiled by Procyon v0.5.36
// 

package com.scheduler.finance.vo;

public class StockPortfolioVo
{
	private String stock_seq;
    private int stock_group;
    private String stock_code;
    private double purchase_price;
    private String target_price;
    private String quantity;
    private String transaction_type;
    private String close_flag;
    private String create_date;
    private String total_quantity;
    private String total_purchase_price;
    private String total_count;
    
	public String getStock_seq() {
		return stock_seq;
	}
	public void setStock_seq(String stock_seq) {
		this.stock_seq = stock_seq;
	}
	public int getStock_group() {
		return stock_group;
	}
	public void setStock_group(int stock_group) {
		this.stock_group = stock_group;
	}
	public String getStock_code() {
		return stock_code;
	}
	public void setStock_code(String stock_code) {
		this.stock_code = stock_code;
	}
	public double getPurchase_price() {
		return purchase_price;
	}
	public void setPurchase_price(double purchase_price) {
		this.purchase_price = purchase_price;
	}
	public String getTarget_price() {
		return target_price;
	}
	public void setTarget_price(String target_price) {
		this.target_price = target_price;
	}
	public String getQuantity() {
		return quantity;
	}
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}
	public String getTransaction_type() {
		return transaction_type;
	}
	public void setTransaction_type(String transaction_type) {
		this.transaction_type = transaction_type;
	}
	public String getClose_flag() {
		return close_flag;
	}
	public void setClose_flag(String close_flag) {
		this.close_flag = close_flag;
	}
	public String getCreate_date() {
		return create_date;
	}
	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}
	public String getTotal_quantity() {
		return total_quantity;
	}
	public void setTotal_quantity(String total_quantity) {
		this.total_quantity = total_quantity;
	}
	public String getTotal_purchase_price() {
		return total_purchase_price;
	}
	public void setTotal_purchase_price(String total_purchase_price) {
		this.total_purchase_price = total_purchase_price;
	}
	public String getTotal_count() {
		return total_count;
	}
	public void setTotal_count(String total_count) {
		this.total_count = total_count;
	}
}
