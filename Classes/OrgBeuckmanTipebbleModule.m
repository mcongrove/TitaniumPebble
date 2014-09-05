/**
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "OrgBeuckmanTipebbleModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

#define MAX_OUTGOING_SIZE 95
#define MESSAGE_KEY @(0x0)

id updateHandler;

@implementation OrgBeuckmanTipebbleModule

#pragma mark Internal

-(id)moduleGUID
{
	return @"01b0607f-455b-4c1e-8f26-a07128d90089";
}

-(NSString*)moduleId
{
	return @"org.beuckman.tipebble";
}

#pragma mark Cleanup 

-(void)dealloc
{
	[connectedWatch closeSession:^{}];
	[super dealloc];
}

#pragma mark Internal Memory Management

-(void)didReceiveMemoryWarning:(NSNotification*)notification
{
	[super didReceiveMemoryWarning:notification];
}

#pragma mark Lifecycle

-(void)startup
{
	NSLog(@"[DEBUG] TiPebble.startup");

	[super startup];

	[[PBPebbleCentral defaultCentral] setDelegate:self];

	connectedWatch = [[PBPebbleCentral defaultCentral] lastConnectedWatch];
}

-(void)pebbleCentral:(PBPebbleCentral*)central watchDidConnect:(PBWatch*)watch isNew:(BOOL)isNew
{
	NSLog(@"[DEBUG] TiPebble.watchDidConnect: %@", [watch name]);

	connectedWatch = watch;

	[self listenToConnectedWatch];

	NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:[watch name], @"name", nil];
	[self fireEvent:@"watchConnected" withObject:event];
}

-(void)pebbleCentral:(PBPebbleCentral*)central watchDidDisconnect:(PBWatch*)watch
{
	NSLog(@"[DEBUG] TiPebble.watchDidDisconnect: %@", [watch name]);

	if(connectedWatch == watch || [watch isEqual:connectedWatch]) {
		[connectedWatch closeSession:^{}];

		connectedWatch = nil;
	}

	NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:[watch name], @"name", nil];
	[self fireEvent:@"watchDisconnected" withObject:event];
}

-(void)listenToConnectedWatch
{
	if(connectedWatch) {
		NSLog(@"[DEBUG] TiPebble.listenToConnectedWatch: Listening");

		if(updateHandler) {
			[connectedWatch appMessagesRemoveUpdateHandler:updateHandler];

			updateHandler = nil;
		}

		updateHandler = [connectedWatch appMessagesAddReceiveUpdateHandler:^BOOL(PBWatch *watch, NSDictionary *message) {
			NSLog(@"[DEBUG] TiPebble.listenToConnectedWatch: Received message");

			[self fireEvent:@"update" withObject:@{ @"message": message[MESSAGE_KEY] }];

			return YES;
		}];
	} else {
		NSLog(@"[WARN] TiPebble.listenToConnectedWatch: No watch connected, not listening");
	}
}

-(void)shutdown:(id)sender
{
	[super shutdown:sender];
}

#pragma Public APIs

-(void)setAppUUID:(id)uuid
{
	ENSURE_SINGLE_ARG(uuid, NSString);

	NSString *uuidString = [TiUtils stringValue:uuid];
	NSUUID *myAppUUID = [[NSUUID alloc] initWithUUIDString:uuidString];
	uuid_t myAppUUIDbytes;

	[myAppUUID getUUIDBytes:myAppUUIDbytes];

	[[PBPebbleCentral defaultCentral] setAppUUID:[NSData dataWithBytes:myAppUUIDbytes length:16]];
}

-(BOOL)checkWatchConnected
{
	if(connectedWatch == nil) {
		NSLog(@"[WARN] TiPebble.checkWatchConnected: No watch connected");

		if(errorCallback != nil) {
			NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:@"No Pebble watch connected.", @"message", nil];
			[self _fireEventToListener:@"error" withObject:event listener:errorCallback thisObject:nil];
		}

		return FALSE;
	} else {
		return TRUE;
	}
}

-(id)connectedCount
{
	NSArray *connected = [[PBPebbleCentral defaultCentral] connectedWatches];

	return NUMINT((int)connected.count);
}

-(void)connect:(id)args
{
	ENSURE_UI_THREAD_1_ARG(args);
	ENSURE_SINGLE_ARG(args, NSDictionary);

	@synchronized(connectedWatch) {
		NSLog(@"[DEBUG] TiPebble.connect");

		id success = [args objectForKey:@"success"];
		id error = [args objectForKey:@"error"];

		RELEASE_TO_NIL(successCallback);
		RELEASE_TO_NIL(errorCallback);

		successCallback = [success retain];
		errorCallback = [error retain];

		[connectedWatch appMessagesGetIsSupported:^(PBWatch *watch, BOOL isAppMessagesSupported) {
			if(!isAppMessagesSupported) {
				NSLog(@"[ERROR] TiPebble.connect: Watch does not support messages");

				if(errorCallback != nil) {
					[self _fireEventToListener:@"error" withObject:nil listener:errorCallback thisObject:nil];
				}

				return;
			}

			NSLog(@"[DEBUG] TiPebble.connect: Messages supported");

			connectedWatch = watch;

			[self listenToConnectedWatch];

			if(successCallback != nil) {
				[self _fireEventToListener:@"success" withObject:nil listener:successCallback thisObject:nil];
			}
		}];
	}
}

-(void)getVersionInfo:(id)args
{
	if(![self checkWatchConnected]) {
		NSLog(@"[WARN] TiPebble.getVersionInfo: No watch connected");

		return;
	}

	ENSURE_UI_THREAD_1_ARG(args);
	ENSURE_SINGLE_ARG(args, NSDictionary);

	@synchronized(connectedWatch) {
		NSLog(@"[DEBUG] TiPebble.getVersionInfo");

		id success = [args objectForKey:@"success"];
		id error = [args objectForKey:@"error"];

		RELEASE_TO_NIL(successCallback);
		RELEASE_TO_NIL(errorCallback);

		successCallback = [success retain];
		errorCallback = [error retain];

		[connectedWatch getVersionInfo:^(PBWatch *watch, PBVersionInfo *versionInfo) {
			NSLog(@"Pebble firmware os version: %li", (long)versionInfo.runningFirmwareMetadata.version.os);
			NSLog(@"Pebble firmware major version: %li", (long)versionInfo.runningFirmwareMetadata.version.major);
			NSLog(@"Pebble firmware minor version: %li", (long)versionInfo.runningFirmwareMetadata.version.minor);
			NSLog(@"Pebble firmware suffix version: %@", versionInfo.runningFirmwareMetadata.version.suffix);

			if(successCallback != nil) {
				NSDictionary *versionInfoDict = [NSDictionary dictionaryWithObjectsAndKeys:
				[NSString stringWithFormat:@"%li", (long)versionInfo.runningFirmwareMetadata.version.os], @"os",
				[NSString stringWithFormat:@"%li", (long)versionInfo.runningFirmwareMetadata.version.major], @"major",
				[NSString stringWithFormat:@"%li", (long)versionInfo.runningFirmwareMetadata.version.minor], @"minor",
				versionInfo.runningFirmwareMetadata.version.suffix, @"suffix", nil];

				[self _fireEventToListener:@"success" withObject:versionInfoDict listener:successCallback thisObject:nil];
			}
		}
		onTimeout:^(PBWatch *watch) {
			NSLog(@"[DEBUG] Timed out trying to get version info from Pebble.");

			if(errorCallback != nil) {
				NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:@"Timed out trying to get version info from Pebble.", @"message",nil];
				[self _fireEventToListener:@"error" withObject:event listener:errorCallback thisObject:nil];
			}
		}
		];
	}
}

-(void)launchApp:(id)args
{
	if(![self checkWatchConnected]) {
		NSLog(@"[WARN] TiPebble.launchApp: No watch connected");

		return;
	}

	ENSURE_UI_THREAD_1_ARG(args);
	ENSURE_SINGLE_ARG(args, NSDictionary);

	@synchronized(connectedWatch) {
		NSLog(@"[DEBUG] TiPebble.launchApp");

		id success = [args objectForKey:@"success"];
		id error = [args objectForKey:@"error"];

		RELEASE_TO_NIL(successCallback);
		RELEASE_TO_NIL(errorCallback);

		successCallback = [success retain];
		errorCallback = [error retain];

		[connectedWatch appMessagesLaunch:^(PBWatch *watch, NSError *error) {
			if(!error) {
				NSLog(@"[DEBUG] TiPebble.launchApp: Success");

				[self listenToConnectedWatch];

				if(successCallback != nil) {
					NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:@"Successfully launched app.", @"message", nil];
					[self _fireEventToListener:@"success" withObject:event listener:successCallback thisObject:nil];
				}
			} else {
				NSLog(@"[ERROR] TiPebble.launchApp: Error");

				if(errorCallback != nil) {
					NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:error.description, @"description", nil];
					[self _fireEventToListener:@"error" withObject:event listener:errorCallback thisObject:nil];
				}
			}
		}];
	}
}

-(void)killApp:(id)args
{
	if(![self checkWatchConnected]) {
		NSLog(@"[WARN] TiPebble.killApp: No watch connected");

		return;
	}

	ENSURE_UI_THREAD_1_ARG(args);
	ENSURE_SINGLE_ARG(args, NSDictionary);

	@synchronized(connectedWatch) {
		NSLog(@"[DEBUG] TiPebble.killApp");

		id success = [args objectForKey:@"success"];
		id error = [args objectForKey:@"error"];

		RELEASE_TO_NIL(successCallback);
		RELEASE_TO_NIL(errorCallback);

		successCallback = [success retain];
		errorCallback = [error retain];

		[connectedWatch appMessagesKill:^(PBWatch *watch, NSError *error) {
			if(!error) {
				NSLog(@"[DEBUG] TiPebble.killApp: Success");
				
				if(successCallback != nil) {
					NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:@"Successfully killed app.", @"message", nil];
					[self _fireEventToListener:@"success" withObject:event listener:successCallback thisObject:nil];
				}
			} else {
				NSLog(@"[ERROR] TiPebble.killApp: Error");

				if(errorCallback != nil) {
					NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:error.description, @"description", nil];
					[self _fireEventToListener:@"error" withObject:event listener:errorCallback thisObject:nil];
				}
			}
		}];
	}
}

-(void)sendMessage:(id)args
{
	if(![self checkWatchConnected]) {
		NSLog(@"[WARN] TiPebble.sendMessage: No watch connected");

		return;
	}

	ENSURE_UI_THREAD_1_ARG(args);
	ENSURE_SINGLE_ARG(args, NSDictionary);

	@synchronized(connectedWatch) {
		NSLog(@"[DEBUG] TiPebble.sendMessage");

		id success = [args objectForKey:@"success"];
		id error = [args objectForKey:@"error"];

		RELEASE_TO_NIL(successCallback);
		RELEASE_TO_NIL(errorCallback);

		successCallback = [success retain];
		errorCallback = [error retain];

		NSDictionary *message = [args objectForKey:@"message"];
		NSMutableDictionary *update = [[NSMutableDictionary alloc] init];
		NSMutableArray *keys = [[message allKeys] mutableCopy];

		for (NSString *key in keys) {
			id obj = [message objectForKey:key];

			NSNumber *updateKey = @([key integerValue]);

			if([obj isKindOfClass:[NSString class]]) {
				NSString *objString = [[NSString alloc] initWithString:obj];

				[update setObject:objString forKey:updateKey];
			}

			if([obj isKindOfClass:[NSNumber class]]) {
				NSNumber *objNumber = [[NSNumber alloc] initWithInteger:[obj integerValue]];

				[update setObject:objNumber forKey:updateKey];
			}
		}

		[connectedWatch appMessagesPushUpdate:update onSent:^(PBWatch *watch, NSDictionary *update, NSError *error) {
			if(!error) {
				NSLog(@"[DEBUG] TiPebble.sendMessage: Success");

				[self _fireEventToListener:@"success" withObject:nil listener:successCallback thisObject:nil];
			} else {
				NSLog(@"[ERROR] TiPebble.sendMessage: Error");

				[self _fireEventToListener:@"error" withObject:error listener:errorCallback thisObject:nil];
			}
		}];
	}
}

@end