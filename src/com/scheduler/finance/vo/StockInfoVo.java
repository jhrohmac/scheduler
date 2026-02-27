// 
// Decompiled by Procyon v0.5.36
// 

package com.scheduler.finance.vo;

import java.sql.Blob;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
public class StockInfoVo
{
    private int stock_seq;
    private String stock_id;
    private String stock_code;
    private String stock_ko_name;
    private String stock_en_name;
    private String stock_market;
    private String stock_type;
    private String stock_type_specs;
    private String stock_currency;
    private String stock_sector;
    private String market_section;
    private String stock_desc;
    private String stock_url;
    private String stock_timezone;
    private String stock_country_code;
    private String stock_aliases;
    private String stock_logo;
    private byte[] imageData;
    private String stock_prevClose;
    private String priceDifference;
    private String pricePercentage;
    private String stock_group;
    private String stock_date;
    private String stock_open;
    private String stock_high;
    private String stock_low;
    private String stock_close;
    private String stock_volume;
    private String stock_price_roi;
    private String stock_flag;
    private String stockAVG_flag;
    private String stock_30minsma;
    private String stock_sma;
    private String stock_mon;
    private String stock_sma5_mon_price;
    private String stock_sma_lines;
    private String stock_5bc;
    private String stock_macd_time;
    private String stock_macd_timedays;
    private String stock_macd_signal;
    private String stock_macd_streaksignal;
    private String stock_macd_streakdays;
    private String use_flag;
    private String watch_close;
    private String watch_date;
    private String modify_date;
    private String modify_state;
    private String bid_rate;
    private String avg_purchase_price;
    private String avg_pricePercentage;
    private String total_quantity;
    private String total_purchase_price;
    private String rnum;
    private int total_count;
    
	public int getStock_seq() {
		return stock_seq;
	}
	public void setStock_seq(int stock_seq) {
		this.stock_seq = stock_seq;
	}
	public String getStock_id() {
		return stock_id;
	}
	public void setStock_id(String stock_id) {
		this.stock_id = stock_id;
	}
	public String getStock_code() {
		return stock_code;
	}
	public void setStock_code(String stock_code) {
		this.stock_code = stock_code;
	}
	public String getStock_ko_name() {
		return stock_ko_name;
	}
	public void setStock_ko_name(String stock_ko_name) {
		this.stock_ko_name = stock_ko_name;
	}
	public String getStock_en_name() {
		return stock_en_name;
	}
	public void setStock_en_name(String stock_en_name) {
		this.stock_en_name = stock_en_name;
	}
	public String getStock_market() {
		return stock_market;
	}
	public void setStock_market(String stock_market) {
		this.stock_market = stock_market;
	}
	public String getStock_type() {
		return stock_type;
	}
	public void setStock_type(String stock_type) {
		this.stock_type = stock_type;
	}
	public String getStock_type_specs() {
		return stock_type_specs;
	}
	public void setStock_type_specs(String stock_type_specs) {
		this.stock_type_specs = stock_type_specs;
	}
	public String getStock_currency() {
		return stock_currency;
	}
	public void setStock_currency(String stock_currency) {
		this.stock_currency = stock_currency;
	}
	public String getStock_sector() {
		return stock_sector;
	}
	public void setStock_sector(String stock_sector) {
		this.stock_sector = stock_sector;
	}
	public String getMarket_section() {
		return market_section;
	}
	public void setMarket_section(String market_section) {
		this.market_section = market_section;
	}
	public String getStock_desc() {
		return stock_desc;
	}
	public void setStock_desc(String stock_desc) {
		this.stock_desc = stock_desc;
	}
	public String getStock_url() {
		return stock_url;
	}
	public void setStock_url(String stock_url) {
		this.stock_url = stock_url;
	}
	public String getStock_timezone() {
		return stock_timezone;
	}
	public void setStock_timezone(String stock_timezone) {
		this.stock_timezone = stock_timezone;
	}
	public String getStock_country_code() {
		return stock_country_code;
	}
	public void setStock_country_code(String stock_country_code) {
		this.stock_country_code = stock_country_code;
	}
	public String getStock_aliases() {
		return stock_aliases;
	}
	public void setStock_aliases(String stock_aliases) {
		this.stock_aliases = stock_aliases;
	}
	public String getStock_logo() {
		return stock_logo;
	}
	public void setStock_logo(String stock_logo) {
		this.stock_logo = stock_logo;
	}
	public byte[] getImageData() {
		return imageData;
	}
	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}
	public String getStock_prevClose() {
		return stock_prevClose;
	}
	public void setStock_prevClose(String stock_prevClose) {
		this.stock_prevClose = stock_prevClose;
	}
	public String getPriceDifference() {
		return priceDifference;
	}
	public void setPriceDifference(String priceDifference) {
		this.priceDifference = priceDifference;
	}
	public String getPricePercentage() {
		return pricePercentage;
	}
	public void setPricePercentage(String pricePercentage) {
		this.pricePercentage = pricePercentage;
	}
	public String getStock_group() {
		return stock_group;
	}
	public void setStock_group(String stock_group) {
		this.stock_group = stock_group;
	}
	public String getStock_date() {
		return stock_date;
	}
	public void setStock_date(String stock_date) {
		this.stock_date = stock_date;
	}
	public String getStock_open() {
		return stock_open;
	}
	public void setStock_open(String stock_open) {
		this.stock_open = stock_open;
	}
	public String getStock_high() {
		return stock_high;
	}
	public void setStock_high(String stock_high) {
		this.stock_high = stock_high;
	}
	public String getStock_low() {
		return stock_low;
	}
	public void setStock_low(String stock_low) {
		this.stock_low = stock_low;
	}
	public String getStock_close() {
		return stock_close;
	}
	public void setStock_close(String stock_close) {
		this.stock_close = stock_close;
	}
	public String getStock_volume() {
		return stock_volume;
	}
	public void setStock_volume(String stock_volume) {
		this.stock_volume = stock_volume;
	}
	public String getStock_flag() {
		return stock_flag;
	}
	public void setStock_flag(String stock_flag) {
		this.stock_flag = stock_flag;
	}
	public String getStockAVG_flag() {
		return stockAVG_flag;
	}
	public void setStockAVG_flag(String stockAVG_flag) {
		this.stockAVG_flag = stockAVG_flag;
	}
	public String getStock_price_roi() {
		return stock_price_roi;
	}
	public void setStock_price_roi(String stock_price_roi) {
		this.stock_price_roi = stock_price_roi;
	}
	public String getStock_30minsma() {
		return stock_30minsma;
	}
	public void setStock_30minsma(String stock_30minsma) {
		this.stock_30minsma = stock_30minsma;
	}
	public String getStock_sma() {
		return stock_sma;
	}
	public void setStock_sma(String stock_sma) {
		this.stock_sma = stock_sma;
	}
	public String getStock_mon() {
		return stock_mon;
	}
	public void setStock_mon(String stock_mon) {
		this.stock_mon = stock_mon;
	}
	public String getStock_sma5_mon_price() {
		return stock_sma5_mon_price;
	}
	public void setStock_sma5_mon_price(String stock_sma5_mon_price) {
		this.stock_sma5_mon_price = stock_sma5_mon_price;
	}
	public String getStock_sma_lines() {
		return stock_sma_lines;
	}
	public void setStock_sma_lines(String stock_sma_lines) {
		this.stock_sma_lines = stock_sma_lines;
	}
	public String getStock_5bc() {
		return stock_5bc;
	}
	public void setStock_5bc(String stock_5bc) {
		this.stock_5bc = stock_5bc;
	}
	public String getStock_macd_time() {
		return stock_macd_time;
	}
	public void setStock_macd_time(String stock_macd_time) {
		this.stock_macd_time = stock_macd_time;
	}
	public String getStock_macd_timedays() {
		return stock_macd_timedays;
	}
	public void setStock_macd_timedays(String stock_macd_timedays) {
		this.stock_macd_timedays = stock_macd_timedays;
	}
	public String getStock_macd_signal() {
		return stock_macd_signal;
	}
	public void setStock_macd_signal(String stock_macd_signal) {
		this.stock_macd_signal = stock_macd_signal;
	}
	public String getStock_macd_streaksignal() {
		return stock_macd_streaksignal;
	}
	public void setStock_macd_streaksignal(String stock_macd_streaksignal) {
		this.stock_macd_streaksignal = stock_macd_streaksignal;
	}
	public String getStock_macd_streakdays() {
		return stock_macd_streakdays;
	}
	public void setStock_macd_streakdays(String stock_macd_streakdays) {
		this.stock_macd_streakdays = stock_macd_streakdays;
	}
	public String getUse_flag() {
		return use_flag;
	}
	public void setUse_flag(String use_flag) {
		this.use_flag = use_flag;
	}
	public String getWatch_close() {
		return watch_close;
	}
	public void setWatch_close(String watch_close) {
		this.watch_close = watch_close;
	}
	public String getWatch_date() {
		return watch_date;
	}
	public void setWatch_date(String watch_date) {
		this.watch_date = watch_date;
	}
	public String getModify_date() {
		return modify_date;
	}
	public void setModify_date(String modify_date) {
		this.modify_date = modify_date;
	}
	public String getModify_state() {
		return modify_state;
	}
	public void setModify_state(String modify_state) {
		this.modify_state = modify_state;
	}
	public String getBid_rate() {
		return bid_rate;
	}
	public void setBid_rate(String bid_rate) {
		this.bid_rate = bid_rate;
	}
	public String getAvg_purchase_price() {
		return avg_purchase_price;
	}
	public void setAvg_purchase_price(String avg_purchase_price) {
		this.avg_purchase_price = avg_purchase_price;
	}
	public String getAvg_pricePercentage() {
		return avg_pricePercentage;
	}
	public void setAvg_pricePercentage(String avg_pricePercentage) {
		this.avg_pricePercentage = avg_pricePercentage;
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
	public String getRnum() {
		return rnum;
	}
	public void setRnum(String rnum) {
		this.rnum = rnum;
	}
	public int getTotal_count() {
		return total_count;
	}
	public void setTotal_count(int total_count) {
		this.total_count = total_count;
	}
	
}
