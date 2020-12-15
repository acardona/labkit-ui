
package net.imglib2.labkit.bdv;

import net.imglib2.Interval;
import net.imglib2.labkit.utils.holder.Holder;
import net.imglib2.labkit.utils.ParametricNotifier;

/**
 * Objects that implement {@link BdvLayer}, can easily be made visible in
 * BigDataViewer using {@link BdvLayerLink}.
 */
public interface BdvLayer {

	Holder<BdvShowable> image();

	ParametricNotifier<Interval> listeners();

	Holder<Boolean> visibility();

	String title();

	class FinalLayer implements BdvLayer {

		private final Holder<BdvShowable> image;
		private final String title;
		private final ParametricNotifier<Interval> listeners =
			new ParametricNotifier<>();
		private final Holder<Boolean> visibility;

		public FinalLayer(Holder<BdvShowable> image, String title,
			Holder<Boolean> visibility)
		{
			this.image = image;
			this.title = title;
			this.visibility = visibility;
		}

		@Override
		public Holder<BdvShowable> image() {
			return image;
		}

		@Override
		public ParametricNotifier<Interval> listeners() {
			return listeners;
		}

		@Override
		public Holder<Boolean> visibility() {
			return visibility;
		}

		@Override
		public String title() {
			return title;
		}
	}
}
