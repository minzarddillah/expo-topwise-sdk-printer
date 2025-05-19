package view;

public class DialogItemObj {
	private int maxLength = 0;// 限制edittext最大可输入长度
	private String hintText = "";// 提示文字
	private boolean isCanEmpty = false;// 是否可为空
	private int limitLength = -1;// 是否限制一定输入字符长度
	private String defaultText = "";// edittext是否有默认值

	public DialogItemObj(int maxLength, String hintText, boolean isCanEmpty,
						 int limitLength, String defaultText) {
		this.maxLength = maxLength;
		this.hintText = hintText;
		this.isCanEmpty = isCanEmpty;
		this.limitLength = limitLength;
		this.defaultText = defaultText;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public String getHintText() {
		return hintText;
	}

	public boolean isCanEmpty() {
		return isCanEmpty;
	}

	public int getLimitLength() {
		return limitLength;
	}

	public String getDefaultText() {
		return defaultText;
	}

}
