package org.kkdev.v2raygo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import org.kkdev.v2raygo.ProxyConfigureFileFragment.OnListFragmentInteractionListener;
import org.kkdev.v2raygo.ProxyFile.ProxyConfigureFile.ProxyConfItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ProxyConfItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyProxyConfigureFileRecyclerViewAdapter extends RecyclerView.Adapter<MyProxyConfigureFileRecyclerViewAdapter.ViewHolder> {

    public  List<ProxyConfItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    private final MyProxyConfigureFileRecyclerViewAdapter me = this;

    public MyProxyConfigureFileRecyclerViewAdapter(List<ProxyConfItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_proxyconfigurefile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.mItem = mValues.get(position);
        //holder.mIdView.setText(mValues.get(position).id);
        //holder.mContentView.setText(mValues.get(position).content);
        holder.MainButton.setText(holder.mItem.ctx.getBriefDesc(holder.mItem.path));
        holder.MainButton.setChecked(holder.mItem.ctx.getConfigureFile().equals(holder.mItem.path));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
        holder.MainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mItem.ctx.assignConfigureFile(holder.mItem.path);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        //public final TextView mIdView;
        //public final TextView mContentView;

        public final RadioButton MainButton;

        public ProxyConfItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            MainButton = (RadioButton)view.findViewById(R.id.Selector);
           // mIdView = (TextView) view.findViewById(R.id.id);
           // mContentView = (TextView) view.findViewById(R.id.content);
        }
        /*
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }*/
    }
}
