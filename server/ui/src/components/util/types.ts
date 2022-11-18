export type SharedKeys<A, B> = Extract<keyof A, keyof B>;
export type ExtendProps<Base, Extension> = Omit<
	Base,
	SharedKeys<Base, Extension>
> &
	Extension;
