/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.brush.neighborhood;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.IterableRegion;
import sc.fiji.labkit.ui.utils.sparse.SparseIterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.Arrays;

public class TransformedSphere {

	private final AffineTransform3D transform;

	public TransformedSphere(AffineTransform3D transform) {
		this.transform = transform;
	}

	public boolean contains(RealLocalizable point) {
		RealPoint out = new RealPoint(3);
		transform.applyInverse(out, point);
		return RealPoints.squaredLength(out) <= 1;
	}

	public RealInterval realBoundingBox() {
		double[] min = new double[3];
		double[] max = new double[3];
		for (int d = 0; d < 3; d++) {
			double halfLength = Math.abs(transform.get(d, 0)) +
				Math.abs(transform.get(d, 1)) +
				Math.abs(transform.get(d, 2)) +
				0.5;
			double center = transform.get(d, 3);
			min[d] = center - halfLength;
			max[d] = center + halfLength;
		}
		return new FinalRealInterval(min, max);
	}

	public Interval boundingBox() {
		RealInterval boundingBox = realBoundingBox();
		long[] min = new long[boundingBox.numDimensions()];
		long[] max = new long[boundingBox.numDimensions()];
		for (int d = 0; d < boundingBox.numDimensions(); d++) {
			min[d] = (long) Math.floor(boundingBox.realMin(d));
			max[d] = (long) Math.ceil(boundingBox.realMax(d));
		}
		return new FinalInterval(min, max);
	}

	// static methods

	public static <T> IterableRegionAsNeighborhood<T> asNeighborhood(
		long position[], AffineTransform3D transformation,
		final RandomAccess<T> source)
	{
		TransformedSphere sphere = new TransformedSphere(transformation);
		IterableRegion<BitType> region = iterableRegion(sphere, source
			.numDimensions());
		IterableRegionAsNeighborhood<T> neighborhood =
			new IterableRegionAsNeighborhood<>(region, source);
		neighborhood.setPosition(position);
		return neighborhood;
	}

	public static IterableRegion<BitType> iterableRegion(TransformedSphere sphere,
		int numDimensions)
	{
		return iterableRegion(sphere, intervalChangeNumDimensions(sphere
			.boundingBox(), numDimensions));
	}

	private static Interval intervalChangeNumDimensions(final Interval interval,
		int numDimensions)
	{
		long[] min = Arrays.copyOf(Intervals.minAsLongArray(interval),
			numDimensions);
		long[] max = Arrays.copyOf(Intervals.maxAsLongArray(interval),
			numDimensions);
		return new FinalInterval(min, max);
	}

	private static IterableRegion<BitType> iterableRegion(
		TransformedSphere sphere, Interval interval)
	{
		SparseIterableRegion result = new SparseIterableRegion(interval);
		Cursor<BitType> cursor = Views.flatIterable(adoptToDimension(result, 3))
			.cursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.get().set(sphere.contains(cursor));
		}
		return result;
	}

	private static <T> RandomAccessibleInterval<T> adoptToDimension(
		RandomAccessibleInterval<T> result, int numDimension)
	{
		while (result.numDimensions() < numDimension)
			result = Views.addDimension(result, 0, 0);
		if (result.numDimensions() > numDimension)
			throw new UnsupportedOperationException();
		return result;
	}
}
