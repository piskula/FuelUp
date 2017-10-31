package sk.momosi.fuelup.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import sk.momosi.fuelup.R;

/**
 * Created by Martin Styk on 10.07.2017.
 */
public class FaqItemView extends LinearLayout implements View.OnClickListener{

    private TextView questionView;
    private TextView answerView;
    private ImageView iconView;

    private String question;
    private String answer;
    private Drawable iconSource;

    public FaqItemView(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.faqItem);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FaqItem, 0, 0);
        question = a.getString(R.styleable.FaqItem_question);
        answer = a.getString(R.styleable.FaqItem_answer);
        a.recycle();

        iconSource = getResources().getDrawable(R.drawable.ic_keyboard_arrow_left_black_24dp);

        setOrientation(VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_faq_item, this, true);

        questionView = findViewById(R.id.faq_question);
        questionView.setText(question);

        answerView = findViewById(R.id.faq_answer);
        answerView.setText(answer);

        iconView = findViewById(R.id.faq_icon);
        iconView.setImageDrawable(iconSource);

        setOnClickListener(this);
    }

    public FaqItemView(Context context) {
        this(context, null);
    }

    public void setQuestion(String question) {
        this.question = question;
        questionView.setText(question);
    }

    public void setAnswer(String answer) {
        this.answer = answer;
        answerView.setText(answer);
    }

    public void setIcon(Drawable icon) {
        this.iconSource = icon;
        iconView.setImageDrawable(iconSource);
    }

    public void onClick(View v) {
        if (answerView.getVisibility() == View.VISIBLE) {
            answerView.setVisibility(View.GONE);
            iconView.setImageResource(R.drawable.ic_keyboard_arrow_left_black_24dp);
        } else {
            answerView.setVisibility(View.VISIBLE);
            iconView.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
        }
    }
}
