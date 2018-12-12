package com.orbbec.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.orbbec.base.BaseApplication;
import com.orbbec.keyguard.R;
import com.orbbec.model.User;
import com.orbbec.utils.GlideUtil;

import java.util.ArrayList;

/**
 * RecycleView的适配器
 */
public class UserListAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<User> data;
    private OnItemClickListener mOnItemClickListener;

    public UserListAdapter() {

    }

    public UserListAdapter(Context context, ArrayList<User> data) {
        this.context = context;
        this.data = data;
    }

    public UserListAdapter(ArrayList<User> data) {
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new UserListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final UserListViewHolder userListViewHolder = (UserListViewHolder) holder;
        userListViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onClick(position);
                }
            }
        });
        final User user = data.get(position);
        userListViewHolder.tv_desc.setText("Face ID=" + user.getPersonId() + ", " + user.getName());
        if (!TextUtils.isEmpty(user.getHead())) {
            GlideUtil.load(BaseApplication.getContext(), userListViewHolder.iv_head, user.getHead());
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateData(ArrayList<User> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * ViewHolder
     */
    static class UserListViewHolder extends RecyclerView.ViewHolder {
        private CardView cv_list_item;
        private ImageView iv_head;
        private TextView tv_desc;

        private UserListViewHolder(View view) {
            super(view);
            cv_list_item = (CardView) view.findViewById(R.id.cv_list_item);
            iv_head = (ImageView) view.findViewById(R.id.iv_head);
            tv_desc = (TextView) view.findViewById(R.id.tv_desc);
        }
    }
}
