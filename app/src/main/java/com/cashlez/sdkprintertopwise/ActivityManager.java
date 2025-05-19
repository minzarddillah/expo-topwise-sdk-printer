package com.cashlez.sdkprintertopwise;

import android.app.Activity;

import java.util.Stack;


public class ActivityManager {
	private static Stack<Activity> activityStack;
	private static ActivityManager instance;
	
	/**实例化一个activity管理类
	 * @return
	 */
	public static synchronized ActivityManager getActivityManager(){
		
		if(instance ==null){
			instance = new ActivityManager();
			activityStack = new Stack<Activity>();
		}
		return instance;		
	}
	
	/**把activity加入到管理类堆栈底,如果已经存在堆栈中将推入栈顶
	 * @param activity
	 */
	public void addActivity(Activity activity){
		if(activity!=null){
			int i = activityStack.indexOf(activity);
			if(i!=-1){
				activityStack.add(activity);
			}else{
				activityStack.push(activity);
			}
			
		}		
	}
	
	
	/**返回堆栈中activity的数量
	 * @return
	 */
	public int getActivityNum(){
		return activityStack.size();	
	}
	
	
	/**将activity推入栈顶
	 * @param activity
	 */
	public void pushActivity(Activity activity)
	  {
	    int i = activityStack.indexOf(activity);
	    if (i != -1)
	    {
	      activityStack.remove(i);
	      activityStack.push(activity);
	    }
	  }
	
	/**获得栈顶的activity
	 * @return
	 */
	public Activity getTopActivity(){
		if((activityStack != null) && (activityStack.size() > 0)){
			return activityStack.lastElement();
		}else{
			return null;
		}		
	} 
	/**退出栈中指定activity
	 * @param activity
	 */
	public void removeActivity(Activity activity){
		if(activity!=null){
			int i = activityStack.indexOf(activity);
			if(i!=-1){
				activity.finish();
				activityStack.remove(activity);				
				System.out.println("remove:"+ activity.getClass().getName());
				System.out.println("activityStack.size:"+activityStack.size());
				activity = null;
			}
		}
	}
	
	/**清除掉除cls以外的其他类
	 * 全部退出的时候要使用removeAllActivity()
	 * @param cls
	 */
	public void removeAllActivityExceptOne(Class<?> cls){
		int size = activityStack.size();
		for(int i =0;i<size;i++){
			Activity activity = getTopActivity();
			if(activity==null){
				break;
			}
			if(activity.getClass().equals(cls)){
				continue;
			}
			removeActivity(activity);
		}
	}
	
	/**
	 * 计算栈中指定类(cls)对象的个数
	 * add by chenkehui @2013.07.10
	 */
	public int activityCount(Class<?> cls){
		int count = 0;
		int size = activityStack.size();
		Activity activity = null;
		for (int i=0; i<size; i++){
			activity = activityStack.get(i);
			if (activity.getClass().equals(cls)) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 退出所有activity
	 */
	public void removeAllActivity(){
		int size = activityStack.size();
		for(int i =0;i<size;i++){
			Activity activity = getTopActivity();
			if(activity!=null){
				removeActivity(activity);
			}
		}
		System.gc();
	}
}
