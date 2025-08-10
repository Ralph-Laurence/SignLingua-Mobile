class StarRatingControl
{
    constructor(selector, readOnly = false, initial = 0)
    {
        this.$container = $(selector);
        
        if (this.$container.length === 0) {
            console.warn(`Star rating control not found: ${selector}`);
            return;
        }

        this.$stars = this.$container.find('.fa-star');
        this.$ratingInput = this.$container.find('.rating-value');
        this.defaultRating = initial; //parseInt(this.$ratingInput.val()) || 0; // Store default rating
        this.isReadOnly = readOnly;

        if (!this.isReadOnly)
        {
            this.enableInteraction();
        }
    }

    /**
     * Sets the star rating programmatically.
     * @param {number} rating - The rating value to set (1-5).
     */
    drawRating(rating) {
        if (!this.$stars || !this.$ratingInput) {
            console.warn("Star rating elements are not initialized.");
            return;
        }

        this.$ratingInput.val(rating); // Update hidden input value

        this.$stars.each(function () {
            const starValue = $(this).data('rating');
            $(this).toggleClass('text-color-primary-700', starValue <= rating);
        });
    }

    /**
     * Retrieves the current rating value.
     * @returns {number} The selected star rating.
     */
    getValue() {
        return parseInt(this.$ratingInput.val()) || 0;
    }

    /**
     * Resets the rating to its default value.
     */
    reset() {
        this.drawRating(this.defaultRating);
    }

    /**
     * Disables user interaction (locks the rating control).
     */
    lock() {
        this.isReadOnly = true;
        this.$stars.off('click'); // Remove click event
        this.$stars.addClass('opacity-50');
    }

    /**
     * Enables user interaction (unlocks the rating control).
     */
    unlock() {
        this.isReadOnly = false;
        this.enableInteraction();
        this.$stars.removeClass('opacity-50');
    }

    /**
     * Helper function to enable interaction.
     */
    enableInteraction() {
        this.$stars.on('click', (event) => {
            const rating = $(event.currentTarget).data('rating');
            this.drawRating(rating);
        });
    }

    setInitial(v) {
        this.defaultRating = v;
    }
}