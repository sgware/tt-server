package edu.uky.cs.nil.tt.world;

import java.util.function.Function;

import edu.uky.cs.nil.tt.Utilities;

/**
 * A signed asset is an {@link Asset asset} which has a unique {@link Signature
 * signature} among all other assets of that type in a {@link World story
 * world}. A signed asset's {@link Asset#getName() name} is equivalent to the
 * result of its signature's {@link Signature#toString() toString()} method.
 * 
 * @author Stephen G. Ware
 */
public abstract class SignedAsset extends Asset {
	
	/** The asset's unique signature */
	public final Signature signature;
	
	/**
	 * Constructs a new signed asset.
	 * 
	 * @param id the asset's unique ID number
	 * @param signature the asset's unique signature
	 * @param description the asset's description
	 */
	public SignedAsset(int id, Signature signature, String description) {
		super(id, signature.toString(), description);
		Utilities.requireNonNull(signature, "signature");
		this.signature = signature;
	}
	
	/**
	 * Returns a new signed asset that is the same as this asset, except that
	 * its signature and name are the given value.
	 * 
	 * @param signature the signature and name the new asset should have
	 * @return a signed asset identical to this asset, except with the given
	 * signature and name
	 */
	protected abstract SignedAsset setSignature(Signature signature);
	
	@Override
	public abstract SignedAsset substitute(Function<Object, Object> substitution);
}