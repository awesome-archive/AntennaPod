package de.danoeh.antennapod.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.feed.FeedPreferences;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.databinding.EditTagsDialogBinding;
import de.danoeh.antennapod.view.ItemOffsetDecoration;

import java.util.ArrayList;
import java.util.List;

public class TagSettingsDialog extends DialogFragment {
    public static final String TAG = "TagSettingsDialog";
    private static final String ARG_FEED_PREFERENCES = "feed_preferences";
    private List<String> displayedTags;

    public static TagSettingsDialog newInstance(FeedPreferences preferences) {
        TagSettingsDialog fragment = new TagSettingsDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FEED_PREFERENCES, preferences);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        FeedPreferences preferences = (FeedPreferences) getArguments().getSerializable(ARG_FEED_PREFERENCES);
        displayedTags = new ArrayList<>(preferences.getTags());
        displayedTags.remove(FeedPreferences.TAG_ROOT);

        EditTagsDialogBinding viewBinding = EditTagsDialogBinding.inflate(getLayoutInflater());
        viewBinding.tagsRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        viewBinding.tagsRecycler.addItemDecoration(new ItemOffsetDecoration(getContext(), 4));
        TagSelectionAdapter adapter = new TagSelectionAdapter();
        adapter.setHasStableIds(true);
        viewBinding.tagsRecycler.setAdapter(adapter);
        viewBinding.rootFolderCheckbox.setChecked(preferences.getTags().contains(FeedPreferences.TAG_ROOT));


        viewBinding.newTagButton.setOnClickListener(v -> {
            String name = viewBinding.newTagEditText.getText().toString().trim();
            if (TextUtils.isEmpty(name) || displayedTags.contains(name)) {
                return;
            }
            displayedTags.add(name);
            viewBinding.newTagEditText.setText("");
            adapter.notifyDataSetChanged();
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setView(viewBinding.getRoot());
        dialog.setTitle(R.string.feed_folders_label);
        dialog.setPositiveButton(android.R.string.ok, (d, input) -> {
            preferences.getTags().clear();
            preferences.getTags().addAll(displayedTags);
            if (viewBinding.rootFolderCheckbox.isChecked()) {
                preferences.getTags().add(FeedPreferences.TAG_ROOT);
            }
            DBWriter.setFeedPreferences(preferences);
        });
        dialog.setNegativeButton(R.string.cancel_label, null);
        return dialog.create();
    }

    public class TagSelectionAdapter extends RecyclerView.Adapter<TagSelectionAdapter.ViewHolder> {

        @Override
        @NonNull
        public TagSelectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Chip chip = new Chip(getContext());
            chip.setCloseIconVisible(true);
            chip.setCloseIconResource(R.drawable.ic_delete);
            return new TagSelectionAdapter.ViewHolder(chip);
        }

        @Override
        public void onBindViewHolder(@NonNull TagSelectionAdapter.ViewHolder holder, int position) {
            holder.chip.setText(displayedTags.get(position));
            holder.chip.setOnCloseIconClickListener(v -> {
                displayedTags.remove(position);
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return displayedTags.size();
        }

        @Override
        public long getItemId(int position) {
            return displayedTags.get(position).hashCode();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            Chip chip;

            ViewHolder(Chip itemView) {
                super(itemView);
                chip = itemView;
            }
        }
    }
}
