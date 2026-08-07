# Accessibility Notes

Coconut Chunks intentionally avoids fixed-height content areas for text-heavy screens. Add/Edit, Groups, Overview, and Settings use scrollable layouts so larger font scales can expand naturally.

Round 4 adds semantic heading information to the app title, explicit descriptions for important icon-only controls, and semantic descriptions for Review status badges.

Review does not depend on swipe gestures: Special, Mastered, and keep-status buttons remain available after reveal. Swipe-right is optional and can be disabled in Settings.

Before stable release, test TalkBack and 200% font scaling on a real device because automated Compose semantics tests cannot fully reproduce spoken navigation quality or OEM font rendering.
