package com.vijay.jsonwizard.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.utils.FormUtils;
import com.vijay.jsonwizard.utils.ImageUtils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.CustomTextView;
import com.vijay.jsonwizard.views.JsonFormFragmentView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class NumberSelectorFactory implements FormWidgetFactory {
    private static CustomTextView selectedTextView;
    private static int selectedItem = -1;
    private static SpinnerOnItemSelected spinnerOnItemSelected = new SpinnerOnItemSelected();

    @SuppressLint("NewApi")
    private static void setSelectedColor(Context context, CustomTextView customTextView, int item, int numberOfSelectors, String textColor) {
        if (customTextView != null && item > -1) {
            if (item == 0) {
                customTextView.setBackground(context.getResources().getDrawable(R.drawable.number_selector_left_rounded_background_selected));
            } else if (item == numberOfSelectors - 1) {
                customTextView.setBackground(context.getResources().getDrawable(R.drawable.number_selector_right_rounded_background_selected));
            } else {
                customTextView.setBackground(context.getResources().getDrawable(R.drawable.number_selector_normal_background_selected));
            }

            customTextView.setTextColor(Color.parseColor(textColor));
        }
    }

    @SuppressLint("NewApi")
    private static void setDefaultColor(Context context, CustomTextView customTextView, int item, int numberOfSelectors, String textColor) {
        if (customTextView != null && item > -1) {
            if (item == 0) {
                customTextView.setBackground(context.getResources().getDrawable(R.drawable.number_selector_left_rounded_background));
            } else if (item == numberOfSelectors - 1) {
                customTextView.setBackground(context.getResources().getDrawable(R.drawable.number_selector_right_rounded_background));
            } else {
                customTextView.setBackground(context.getResources().getDrawable(R.drawable.number_selector_normal_background));
            }
            customTextView.setTextColor(Color.parseColor(textColor));
        }
    }

    public static void createNumberSelector(CustomTextView textView) {
        int item = (int) textView.getTag(R.id.number_selector_textview_item);
        int numberOfSelectors = (int) textView.getTag(R.id.number_selector_textview_number_of_selectors);
        int maxValue = (int) textView.getTag(R.id.number_selector_textview_max_number);
        String defaultColor = (String) textView.getTag(R.id.number_selector_default_text_color);
        String selectedColor = (String) textView.getTag(R.id.number_selector_selected_text_color);

        if (item == numberOfSelectors - 1 && numberOfSelectors - 1 < maxValue) {
            LinearLayout linearLayout = (LinearLayout) textView.getTag(R.id.number_selector_textview_layout);
            JSONObject jsonObject = (JSONObject) textView.getTag(R.id.number_selector_textview_jsonObject);
            NumberSelectorFactory.createDialogSpinner(linearLayout, textView.getContext(), jsonObject, numberOfSelectors);
        }

        if (selectedTextView != textView) {
            setSelectedColor(textView.getContext(), textView, item, numberOfSelectors, selectedColor);
            setDefaultColor(textView.getContext(), selectedTextView, selectedItem, numberOfSelectors, defaultColor);
            selectedTextView = textView;
            selectedItem = item;
        }

        if (item < numberOfSelectors - 1) {
            Log.d(TAG, "Selected: " + textView.getText().toString());
            Toast.makeText(textView.getContext(), "Selected: " + textView.getText().toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static void createDialogSpinner(LinearLayout linearLayout, Context context, JSONObject jsonObject, int startNumber) {
        int maxValue = jsonObject.optInt(JsonFormConstants.MAX_SELECTION_VALUE, 20);
        LinearLayout.LayoutParams layoutParams = FormUtils.getLinearLayoutParams(10, FormUtils.WRAP_CONTENT, 1, 1, 1, 1);
        Spinner spinner = new Spinner(context, Spinner.MODE_DIALOG);
        List<Integer> numbers = new ArrayList<>();
        for (int i = startNumber; i <= maxValue; i++) {
            numbers.add(i);
        }

        ArrayAdapter<Integer> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, numbers);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            spinner.setDropDownWidth(100);
        }
        spinner.setLayoutParams(layoutParams);
        spinner.performClick();
        spinner.setOnItemSelectedListener(spinnerOnItemSelected);
        linearLayout.addView(spinner);

    }

    public static ValidationStatus validate(JsonFormFragmentView formFragmentView,
                                            CustomTextView customTextView) {
        if (!(customTextView.getTag(R.id.v_required) instanceof String) || !(customTextView.getTag(R.id.error) instanceof String)) {
            return new ValidationStatus(true, null, formFragmentView, customTextView);
        }
        Boolean isRequired = Boolean.valueOf((String) customTextView.getTag(R.id.v_required));
        if (!isRequired || !customTextView.isEnabled()) {
            return new ValidationStatus(true, null, formFragmentView, customTextView);
        }
        String selectedNumber = String.valueOf(selectedTextView.getText());
        if (!selectedNumber.isEmpty()) {
            return new ValidationStatus(true, null, formFragmentView, customTextView);
        } else {
            return new ValidationStatus(false, (String) customTextView.getTag(R.id.error), formFragmentView, customTextView);
        }
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener
            listener) throws Exception {
        List<View> views = new ArrayList<>(1);
        JSONArray canvasIds = new JSONArray();
        String openMrsEntityParent = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.getString(JsonFormConstants.OPENMRS_ENTITY_ID);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);

        LinearLayout rootLayout = (LinearLayout) LayoutInflater.from(context).inflate(getLayout(), null);

        rootLayout.setId(ViewUtil.generateViewId());
        rootLayout.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        rootLayout.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        rootLayout.setTag(R.id.openmrs_entity, openMrsEntity);
        rootLayout.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        rootLayout.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        rootLayout.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        canvasIds.put(rootLayout.getId());
        rootLayout.setTag(R.id.canvas_ids, canvasIds.toString());
        if (relevance != null && context instanceof JsonApi) {
            rootLayout.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(rootLayout);
        }
        views.add(rootLayout);
        createTextViews(context, jsonObject, rootLayout, listener);

        return views;
    }

    @SuppressLint("NewApi")
    private void createTextViews(Context context, JSONObject jsonObject, LinearLayout linearLayout, CommonListener listener) throws JSONException {
        int width = ImageUtils.getDeviceWidth(context);
        width = (int) (width - context.getResources().getDimension(R.dimen.native_selector_total_screen_size_padding));

        int numberOfSelectors = jsonObject.optInt(JsonFormConstants.NUMBER_OF_SELECTORS, 5);
        int startSelectionNumber = jsonObject.optInt(JsonFormConstants.START_SELECTION_NUMBER, 1);
        int maxValue = jsonObject.optInt(JsonFormConstants.MAX_SELECTION_VALUE, 20);
        String textColor = jsonObject.optString(JsonFormConstants.TEXT_COLOR, JsonFormConstants.DEFAULT_TEXT_COLOR);
        String selectedTextColor = jsonObject.optString(JsonFormConstants.NUMBER_SELECTOR_SELCTED_TEXT_COLOR, JsonFormConstants.DEFAULT_NUMBER_SELECTOR_TEXT_COLOR);
        int textSize = jsonObject.optInt(JsonFormConstants.TEXT_SIZE, (int) context.getResources().getDimension(R.dimen.default_text_size));
        LinearLayout.LayoutParams layoutParams = FormUtils.getLinearLayoutParams(width / numberOfSelectors, FormUtils.WRAP_CONTENT, 1, 2, 1, 2);

        for (int i = 0; i < numberOfSelectors; i++) {
            CustomTextView customTextView = FormUtils.getTextViewWith(context, textSize, getText(i, startSelectionNumber,
                    numberOfSelectors, maxValue), jsonObject.getString(JsonFormConstants.KEY),
                    jsonObject.getString("type"), "", "", "",
                    "", layoutParams, FormUtils.FONT_BOLD_PATH, 0, textColor);
            customTextView.setId(R.id.number_selector_textview + i);
            customTextView.setPadding(0, 5, 0, 5);
            setDefaultColor(context, customTextView, i, numberOfSelectors, textColor);
            customTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            customTextView.setClickable(true);
            customTextView.setOnClickListener(listener);
            customTextView.setTag(R.id.number_selector_textview_item, i);
            customTextView.setTag(R.id.number_selector_textview_number_of_selectors, numberOfSelectors);
            customTextView.setTag(R.id.number_selector_textview_max_number, maxValue);
            customTextView.setTag(R.id.number_selector_default_text_color, textColor);
            customTextView.setTag(R.id.number_selector_selected_text_color, selectedTextColor);
            customTextView.setTag(R.id.number_selector_listener, listener);
            customTextView.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
            customTextView.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
            if (i == numberOfSelectors - 1) {
                customTextView.setTag(R.id.number_selector_textview_layout, linearLayout);
                customTextView.setTag(R.id.number_selector_textview_jsonObject, jsonObject);
            }
            linearLayout.addView(customTextView);
        }
    }

    private String getText(int item, int startSelectionNumber, int numberOfSelectors, int maxValue) {
        String text = String.valueOf(startSelectionNumber == 0 ? item : item + 1);
        if (item == numberOfSelectors - 1 && maxValue > Integer.parseInt(text)) {
            text = text + "+";
        }
        return text;
    }

    protected int getLayout() {
        return R.layout.native_form_item_numbers_selector;
    }

    private static class SpinnerOnItemSelected implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String item = adapterView.getItemAtPosition(i).toString();
            selectedTextView.setText(item);
            Log.d(TAG, "Selected: " + selectedTextView.getText().toString());
            Toast.makeText(selectedTextView.getContext(), "Selected: " + selectedTextView.getText().toString(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

}
