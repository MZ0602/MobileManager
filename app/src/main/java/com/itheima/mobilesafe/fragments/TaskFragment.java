package com.itheima.mobilesafe.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itheima.mobilesafe.R;
import com.itheima.mobilesafe.adapter.TaskInfoListAdapter;
import com.itheima.mobilesafe.ui.AutoResizeTextView;
import com.itheima.mobilesafe.ui.recycler_view.DividerItemDecoration;
import com.itheima.mobilesafe.ui.recycler_view.ItemTouchCallback;
import com.itheima.mobilesafe.utils.CLog;
import com.itheima.mobilesafe.utils.SystemInfoUtils;
import com.itheima.mobilesafe.utils.objects.TaskInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Catherine on 2016/9/11.
 * Soft-World Inc.
 * catherine919@soft-world.com.tw
 */
public class TaskFragment extends Fragment {

    private static final String TAG = "TaskFragment";
    private AutoResizeTextView tv_memory_info;
    private TextView tv_progress_count, tv_user_tasks_count, tv_sys_tasks_count, tv_release_all, tv_settings;
    private LinearLayout ll_loading;
    private RecyclerView rv_user_tasks, rv_sys_tasks;
    private List<TaskInfo> returns;
    private TaskInfoListAdapter userAdapter, sysAdapter;
    private ItemTouchHelper userItemTouchHelper, sysItemTouchHelper;

    public static TaskFragment newInstance() {
        return new TaskFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);
        tv_progress_count = (TextView) view.findViewById(R.id.tv_progress_count);
        tv_user_tasks_count = (TextView) view.findViewById(R.id.tv_user_tasks_count);
        tv_user_tasks_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rv_user_tasks.getVisibility() == View.VISIBLE)
                    rv_user_tasks.setVisibility(View.GONE);
                else
                    rv_user_tasks.setVisibility(View.VISIBLE);

            }
        });
        tv_sys_tasks_count = (TextView) view.findViewById(R.id.tv_sys_tasks_count);
        tv_sys_tasks_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rv_sys_tasks.getVisibility() == View.VISIBLE)
                    rv_sys_tasks.setVisibility(View.GONE);
                else
                    rv_sys_tasks.setVisibility(View.VISIBLE);

            }
        });
        tv_memory_info = (AutoResizeTextView) view.findViewById(R.id.tv_memory_info);
        tv_release_all = (TextView) view.findViewById(R.id.tv_release_all);
        tv_release_all.setEnabled(false);
        tv_release_all.setClickable(false);
        tv_release_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> packages = new ArrayList<>();
                for (int i = 0; i < userAdapter.getItemCount(); i++) {
                    packages.add(userAdapter.getItemName(i));
                }
                for (int i = 0; i < sysAdapter.getItemCount(); i++) {
                    packages.add(sysAdapter.getItemName(i));
                }
                SystemInfoUtils.killAllProcess(getActivity(), packages);
                setFalseDate();
            }
        });
        tv_settings = (TextView) view.findViewById(R.id.tv_settings);
        tv_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CLog.d(TAG, "onClick");

            }
        });
        ll_loading = (LinearLayout) view.findViewById(R.id.ll_loading);
        rv_user_tasks = (RecyclerView) view.findViewById(R.id.rv_user_tasks);
        rv_sys_tasks = (RecyclerView) view.findViewById(R.id.rv_sys_tasks);

        //添加分割线
        rv_user_tasks.addItemDecoration(new DividerItemDecoration(
                getActivity(), DividerItemDecoration.VERTICAL_LIST));
        rv_sys_tasks.addItemDecoration(new DividerItemDecoration(
                getActivity(), DividerItemDecoration.VERTICAL_LIST));
        //设置布局管理器,可实现GridVIew
        rv_user_tasks.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        rv_sys_tasks.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        int progressCount = SystemInfoUtils.getRunningProcessCount(getActivity());
        String count = String.format(getResources().getString(R.string.running_process), progressCount);
        tv_progress_count.setText(count);

        long availMen = SystemInfoUtils.getAvailableMemory(getActivity());
        long totalMen = SystemInfoUtils.getTotalMemory(getActivity());
        String text = String.format(getResources().getString(R.string.widget_memory), SystemInfoUtils.formatFileSize(availMen), SystemInfoUtils.formatFileSize(totalMen));
        tv_memory_info.setText(text);
        fillInData();
        return view;
    }

    private List<TaskInfo> userInfo, sysInfo;

    private void fillInData() {
        ll_loading.setVisibility(View.VISIBLE);

        new Thread() {
            public void run() {
                userInfo = new LinkedList<>();
                sysInfo = new LinkedList<>();
                returns = SystemInfoUtils.getTaskInfos(getActivity());

                for (TaskInfo info : returns) {
                    if (info.userTask)
                        userInfo.add(info);
                    else
                        sysInfo.add(info);
                }

                userAdapter = new TaskInfoListAdapter(getActivity(), userInfo);
                userAdapter.setOnItemClickLitener(new TaskInfoListAdapter.OnItemClickLitener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        CLog.d(TAG, "onItemClick");
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        CLog.d(TAG, "onItemLongClick");

                    }
                });
                userAdapter.setOnItemMoveLitener(new TaskInfoListAdapter.OnItemMoveListener() {
                    @Override
                    public void onItemSwap(int fromPosition, int toPosition) {
                        CLog.d(TAG, "from " + fromPosition + " to " + toPosition);
                    }

                    @Override
                    public void onItemSwipe(int position) {
                        CLog.d(TAG, userAdapter.getItemName(position));
                        SystemInfoUtils.killProcess(getActivity(), userAdapter.getItemName(position));
                        refresh(false);
                    }
                });
                sysAdapter = new TaskInfoListAdapter(getActivity(), sysInfo);
                sysAdapter.setOnItemClickLitener(new TaskInfoListAdapter.OnItemClickLitener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        CLog.d(TAG, "onItemClick");

                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        CLog.d(TAG, "onItemLongClick");

                    }
                });
                sysAdapter.setOnItemMoveLitener(new TaskInfoListAdapter.OnItemMoveListener() {
                    @Override
                    public void onItemSwap(int fromPosition, int toPosition) {
                        CLog.d(TAG, "from " + fromPosition + " to " + toPosition);
                    }

                    @Override
                    public void onItemSwipe(int position) {
                        CLog.d(TAG, position + "");
                        CLog.d(TAG, sysAdapter.getItemName(position));
                        SystemInfoUtils.killProcess(getActivity(), sysAdapter.getItemName(position));
                        refresh(false);
                    }
                });
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ll_loading.setVisibility(View.GONE);
                            tv_release_all.setEnabled(true);
                            tv_release_all.setClickable(true);

                            String userPCount = String.format(getResources().getString(R.string.user_running_process), userAdapter.getItemCount());
                            String sysPCount = String.format(getResources().getString(R.string.sys_running_process), sysAdapter.getItemCount());
                            tv_user_tasks_count.setText(userPCount);
                            tv_sys_tasks_count.setText(sysPCount);

                            if (userAdapter.getItemCount() == 0)
                                tv_user_tasks_count.setVisibility(View.GONE);
                            else
                                tv_user_tasks_count.setVisibility(View.VISIBLE);
                            if (sysAdapter.getItemCount() == 0)
                                tv_sys_tasks_count.setVisibility(View.GONE);
                            else
                                tv_sys_tasks_count.setVisibility(View.VISIBLE);

                            rv_user_tasks.setAdapter(userAdapter);
                            userItemTouchHelper = new ItemTouchHelper(new ItemTouchCallback(userAdapter));
                            userItemTouchHelper.attachToRecyclerView(rv_user_tasks);

                            rv_sys_tasks.setAdapter(sysAdapter);
                            sysItemTouchHelper = new ItemTouchHelper(new ItemTouchCallback(sysAdapter));
                            sysItemTouchHelper.attachToRecyclerView(rv_sys_tasks);


                            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.tran_in);
//                        tv_user_tasks_count.startAnimation(animation);
//                        tv_sys_tasks_count.startAnimation(animation);
                            rv_user_tasks.startAnimation(animation);
                            rv_sys_tasks.startAnimation(animation);
                        }
                    });
                }
            }
        }.start();

    }

    private void refresh(boolean refreshList) {
        int progressCount = SystemInfoUtils.getRunningProcessCount(getActivity());
        String count = String.format(getResources().getString(R.string.running_process), progressCount);
        tv_progress_count.setText(count);

        long availMen = SystemInfoUtils.getAvailableMemory(getActivity());
        long totalMen = SystemInfoUtils.getTotalMemory(getActivity());
        String text = String.format(getResources().getString(R.string.widget_memory), SystemInfoUtils.formatFileSize(availMen), SystemInfoUtils.formatFileSize(totalMen));
        tv_memory_info.setText(text);

        String userPCount = String.format(getResources().getString(R.string.user_running_process), userAdapter.getItemCount());
        String sysPCount = String.format(getResources().getString(R.string.sys_running_process), sysAdapter.getItemCount());
        tv_user_tasks_count.setText(userPCount);
        tv_sys_tasks_count.setText(sysPCount);

        if (refreshList)
            fillInData();
    }

    public void setFalseDate() {
        String count = String.format(getResources().getString(R.string.running_process), 1);
        tv_progress_count.setText(count);
        long availMen = SystemInfoUtils.getAvailableMemory(getActivity());
        long totalMen = SystemInfoUtils.getTotalMemory(getActivity());
        String text = String.format(getResources().getString(R.string.widget_memory), SystemInfoUtils.formatFileSize(availMen), SystemInfoUtils.formatFileSize(totalMen));
        tv_memory_info.setText(text);
        String userPCount = String.format(getResources().getString(R.string.user_running_process), 1);
        String sysPCount = String.format(getResources().getString(R.string.sys_running_process), 0);
        tv_user_tasks_count.setText(userPCount);
        tv_sys_tasks_count.setText(sysPCount);

        ll_loading.setVisibility(View.VISIBLE);

        new Thread() {
            public void run() {
                userInfo = new LinkedList<>();
                sysInfo = new LinkedList<>();
                returns = SystemInfoUtils.getTaskInfos(getActivity());

                for (TaskInfo info : returns) {
                    if (info.userTask && info.packageName.equals(getActivity().getPackageName()))
                        userInfo.add(info);
                }

                userAdapter = new TaskInfoListAdapter(getActivity(), userInfo);
                userAdapter.setOnItemClickLitener(new TaskInfoListAdapter.OnItemClickLitener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        CLog.d(TAG, "onItemClick");
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        CLog.d(TAG, "onItemLongClick");

                    }
                });
                userAdapter.setOnItemMoveLitener(new TaskInfoListAdapter.OnItemMoveListener() {
                    @Override
                    public void onItemSwap(int fromPosition, int toPosition) {
                        CLog.d(TAG, "from " + fromPosition + " to " + toPosition);
                    }

                    @Override
                    public void onItemSwipe(int position) {
                        CLog.d(TAG, userAdapter.getItemName(position));
                        SystemInfoUtils.killProcess(getActivity(), userAdapter.getItemName(position));
                        refresh(false);
                    }
                });
                sysAdapter = new TaskInfoListAdapter(getActivity(), sysInfo);
                sysAdapter.setOnItemClickLitener(new TaskInfoListAdapter.OnItemClickLitener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        CLog.d(TAG, "onItemClick");

                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        CLog.d(TAG, "onItemLongClick");

                    }
                });
                sysAdapter.setOnItemMoveLitener(new TaskInfoListAdapter.OnItemMoveListener() {
                    @Override
                    public void onItemSwap(int fromPosition, int toPosition) {
                        CLog.d(TAG, "from " + fromPosition + " to " + toPosition);
                    }

                    @Override
                    public void onItemSwipe(int position) {
                        CLog.d(TAG, position + "");
                        CLog.d(TAG, sysAdapter.getItemName(position));
                        SystemInfoUtils.killProcess(getActivity(), sysAdapter.getItemName(position));
                        refresh(false);
                    }
                });
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ll_loading.setVisibility(View.GONE);
                            tv_release_all.setEnabled(true);
                            tv_release_all.setClickable(true);
                            String userPCount = String.format(getResources().getString(R.string.user_running_process), 1);
                            tv_user_tasks_count.setText(userPCount);
                            tv_sys_tasks_count.setVisibility(View.GONE);

                            rv_user_tasks.setAdapter(userAdapter);
                            userItemTouchHelper = new ItemTouchHelper(new ItemTouchCallback(userAdapter));
                            userItemTouchHelper.attachToRecyclerView(rv_user_tasks);

                            rv_sys_tasks.setAdapter(sysAdapter);
                            sysItemTouchHelper = new ItemTouchHelper(new ItemTouchCallback(sysAdapter));
                            sysItemTouchHelper.attachToRecyclerView(rv_sys_tasks);


                            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.tran_in);
//                        tv_user_tasks_count.startAnimation(animation);
//                        tv_sys_tasks_count.startAnimation(animation);
                            rv_user_tasks.startAnimation(animation);
                            rv_sys_tasks.startAnimation(animation);
                        }
                    });
                }
            }
        }.start();


    }
}
